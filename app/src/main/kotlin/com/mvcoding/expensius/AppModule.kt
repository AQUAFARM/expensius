/*
 * Copyright (C) 2016 Mantas Varnagiris.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.mvcoding.expensius

import android.content.Context
import com.memoizrlabs.Shank.registerFactory
import com.memoizrlabs.ShankModule
import com.mvcoding.expensius.extension.provideSingleton
import com.mvcoding.expensius.feature.AmountFormatter
import com.mvcoding.expensius.feature.CurrencyFormat
import com.mvcoding.expensius.feature.CurrencyFormat.DecimalSeparator.DOT
import com.mvcoding.expensius.feature.CurrencyFormat.GroupSeparator.COMMA
import com.mvcoding.expensius.feature.CurrencyFormat.SymbolDistance.CLOSE
import com.mvcoding.expensius.feature.CurrencyFormat.SymbolPosition.START
import com.mvcoding.expensius.feature.DateFormatter
import com.mvcoding.expensius.feature.transaction.Currency
import com.mvcoding.expensius.provider.database.DBHelper
import com.mvcoding.expensius.provider.database.Database
import com.mvcoding.expensius.provider.database.SqliteDatabase
import com.mvcoding.expensius.provider.database.table.TagsTable
import com.mvcoding.expensius.provider.database.table.TransactionTagsTable
import com.mvcoding.expensius.provider.database.table.TransactionsTable
import com.squareup.sqlbrite.SqlBrite
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.math.BigDecimal

class AppModule(val context: Context) : ShankModule {

    override fun registerFactories() {
        appContext()
        rxSchedulers()
        rxBus()
        settings()
        session()
        dateFormatter()
        database()
        amountFormatter()
    }

    private fun appContext() = registerFactory(Context::class.java, { -> context })
    private fun rxBus() = registerFactory(RxBus::class.java, { -> RxBus() })
    private fun settings() = registerFactory(Settings::class.java, { -> UserSettings() })
    private fun session() = registerFactory(Session::class.java, { -> UserSession() })
    private fun dateFormatter() = registerFactory(DateFormatter::class.java, { -> DateFormatter(context) })

    private fun rxSchedulers() = registerFactory(RxSchedulers::class.java, { ->
        RxSchedulers(AndroidSchedulers.mainThread(), Schedulers.io())
    })

    private fun database() {
        val briteDatabase = SqlBrite.create().wrapDatabaseHelper(DBHelper(
                context,
                TagsTable(),
                TransactionsTable(),
                TransactionTagsTable()),
                Schedulers.io())
        registerFactory(Database::class.java, { -> SqliteDatabase(briteDatabase) })
    }

    private fun amountFormatter() {
        // TODO: This is temporary
        registerFactory(AmountFormatter::class.java, { ->
            object : AmountFormatter {
                private val currencyFormat = CurrencyFormat("£", START, CLOSE, DOT, COMMA, 2, 2)
                override fun format(amount: BigDecimal, currency: Currency) = currencyFormat.format(amount)
            }
        })
    }
}

fun provideContext() = provideSingleton(Context::class)
fun provideRxSchedulers() = provideSingleton(RxSchedulers::class)
fun provideSettings() = provideSingleton(Settings::class)
fun provideSession() = provideSingleton(Session::class)
fun provideDatabase() = provideSingleton(Database::class)