package com.financialtracker.backend.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_shouldCreateCategorySuccessfully() {

        CategoryRequest request = new CategoryRequest();
        request.setName("Groceries");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Groceries");
        savedCategory.setActive(true);

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        CategoryResponse response =
                categoryService.createCategory(request);

        assertEquals(1L, response.getId());
        assertEquals("Groceries", response.getName());
        assertTrue(response.isActive());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void getAllCategories_shouldReturnAllCategories() {

        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Food & Groceries");
        category1.setActive(true);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Rent");
        category2.setActive(true);

        when(categoryRepository.findAll())
                .thenReturn(java.util.List.of(category1, category2));

        var response = categoryService.getAllCategories();

        assertEquals(2, response.size());

        assertEquals(1L, response.get(0).getId());
        assertEquals("Food & Groceries", response.get(0).getName());
        assertTrue(response.get(0).isActive());

        assertEquals(2L, response.get(1).getId());
        assertEquals("Rent", response.get(1).getName());
        assertTrue(response.get(1).isActive());

        verify(categoryRepository).findAll();
    }
    @Test
    void getCategoryById_shouldReturnCategorySuccessfully() {

        Category category = new Category();
        category.setId(1L);
        category.setName("Food & Groceries");
        category.setActive(true);

        when(categoryRepository.findById(1L))
                .thenReturn(java.util.Optional.of(category));

        CategoryResponse response =
                categoryService.getCategoryById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Food & Groceries", response.getName());
        assertTrue(response.isActive());

        verify(categoryRepository).findById(1L);
    }
    @Test
    void getCategoryById_shouldThrowExceptionWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> categoryService.getCategoryById(999L)
                );

        assertEquals(
                "Category with id 999 not found",
                exception.getMessage()
        );

        verify(categoryRepository).findById(999L);
    }

    @Test
    void updateCategory_shouldUpdateCategorySuccessfully() {

        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Groceries");
        existingCategory.setActive(true);

        CategoryRequest request = new CategoryRequest();
        request.setName("Food & Groceries");

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Food & Groceries");
        updatedCategory.setActive(true);

        when(categoryRepository.findById(1L))
                .thenReturn(java.util.Optional.of(existingCategory));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(updatedCategory);

        CategoryResponse response =
                categoryService.updateCategory(1L, request);

        assertEquals(1L, response.getId());
        assertEquals("Food & Groceries", response.getName());
        assertTrue(response.isActive());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(existingCategory);
    }

    @Test
    void updateCategory_shouldThrowExceptionWhenCategoryDoesNotExist() {

        CategoryRequest request = new CategoryRequest();
        request.setName("Food & Groceries");

        when(categoryRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> categoryService.updateCategory(999L, request)
                );

        assertEquals(
                "Category with id 999 not found",
                exception.getMessage()
        );

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deactivateCategory_shouldDeactivateCategorySuccessfully() {

        Category category = new Category();
        category.setId(1L);
        category.setName("Food & Groceries");
        category.setActive(true);

        Category deactivatedCategory = new Category();
        deactivatedCategory.setId(1L);
        deactivatedCategory.setName("Food & Groceries");
        deactivatedCategory.setActive(false);

        when(categoryRepository.findById(1L))
                .thenReturn(java.util.Optional.of(category));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(deactivatedCategory);

        CategoryResponse response =
                categoryService.deactivateCategory(1L);

        assertEquals(1L, response.getId());
        assertEquals("Food & Groceries", response.getName());
        assertFalse(response.isActive());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(category);
    }
    @Test
    void deactivateCategory_shouldThrowExceptionWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> categoryService.deactivateCategory(999L)
                );

        assertEquals(
                "Category with id 999 not found",
                exception.getMessage()
        );

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_shouldRejectDuplicateCategoryName() {

        CategoryRequest request = new CategoryRequest();
        request.setName("Groceries");

        when(categoryRepository.existsByNameIgnoreCase("Groceries"))
                .thenReturn(true);

        CategoryAlreadyExistsException exception =
                assertThrows(
                        CategoryAlreadyExistsException.class,
                        () -> categoryService.createCategory(request)
                );

        assertEquals(
                "Category with name 'Groceries' already exists",
                exception.getMessage()
        );

        verify(categoryRepository)
                .existsByNameIgnoreCase("Groceries");

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

}
