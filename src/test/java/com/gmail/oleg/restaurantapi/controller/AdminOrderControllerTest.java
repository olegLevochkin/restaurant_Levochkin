package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.Product;
import com.gmail.oleg.restaurantapi.service.DishService;
import com.gmail.oleg.restaurantapi.service.OrderService;
import com.gmail.oleg.restaurantapi.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AdminOrderControllerTest {

    @Mock
    private DishService dishService;

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private Model model;

    @InjectMocks
    private AdminOrderController testingInstance;

    @Test
    public void adminOrderShouldReturnCorrectURL() {
        final Product product = new Product(1L, "Cabbage", 10, 500, 50, 10, Collections.emptySet(), Collections.emptyList());

        given(productService.getAllProducts()).willReturn(Arrays.asList(product));


        assertThat(testingInstance.adminOrder(model)).isEqualTo("redirect:/adminOrder");
        verify(orderService).confirmToAdmin();
        verify(productService, atLeast(2)).getAllProducts();
        assertThat(testingInstance.adminOrder(model)).isEqualTo("menu/adminOrder");
    }

    @Test
    public void replenishShouldReplenishProducts() {
        final Product product = new Product(1L, "Cabbage", 500, 500, 50, 30, Collections.emptySet(), Collections.emptyList());

        given(productService.getAllProducts()).willReturn(Arrays.asList(product, product));

        testingInstance.replenish();
        verify(productService).getAllProducts();
        verify(productService, times(2)).saveProduct(any());
        assertThat(testingInstance.replenish()).isEqualTo("redirect:/adminOrder");
    }

    @Test
    public void checkOrderShouldReturnCheckPage() {
        final Long id = 2L;

        final ArgumentCaptor acLong = ArgumentCaptor.forClass(Long.class);
        final ArgumentCaptor acString = ArgumentCaptor.forClass(String.class);

        testingInstance.checkOrder(id, model);
        verify(model, times(3)).addAttribute((String) acString.capture(), acLong.capture());
        assertEquals(List.of(id, "orderID"), Arrays.asList(acLong.getAllValues().get(0), acString.getAllValues().get(0)));

        verify(dishService).findByOrderID(id);
        verify(productService).getAllProductsFromOrder(id);
        assertThat(testingInstance.checkOrder(id, model)).isEqualTo("menu/checkPage");
    }

    @Test
    public void confirmShouldReturnAdminPage() {
        final Long id = 2L;

        testingInstance.confirm(id);
        verify(orderService).confirm(id);
        assertThat(testingInstance.confirm(id)).isEqualTo("redirect:/adminOrder");
    }

}
