package com.mjtech.store.data.mock.di

import com.mjtech.store.data.mock.cart.repository.MockCartRepository
import com.mjtech.store.data.mock.payment.repository.MockPaymentRepository
import com.mjtech.store.data.mock.products.repository.MockProductsRepository
import com.mjtech.store.domain.cart.repostitory.CartRepository
import com.mjtech.store.domain.payment.repository.PaymentRepository
import com.mjtech.store.domain.products.repository.ProductsRepository
import org.koin.dsl.module

val mockDataModule = module {

    single<ProductsRepository> { MockProductsRepository(get()) }

    single<CartRepository> { MockCartRepository() }

    single<PaymentRepository> { MockPaymentRepository() }
}