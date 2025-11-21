package com.mjtech.fiserv.msitef.payment

import android.content.Context
import android.content.Intent
import android.util.Log
import com.mjtech.fiserv.msitef.common.getCurrentDate
import com.mjtech.fiserv.msitef.common.getCurrentTime
import com.mjtech.fiserv.msitef.common.toStringWithoutDots
import com.mjtech.store.domain.payment.model.Payment
import com.mjtech.store.domain.payment.model.PaymentType
import com.mjtech.store.domain.payment.repository.PaymentCallback
import com.mjtech.store.domain.payment.repository.PaymentProcessor

internal class MSitefPaymentProcessor(private val context: Context) : PaymentProcessor {

    override fun processPayment(
        payment: Payment,
        callback: PaymentCallback
    ) {
        val modalidade = mapPaymentMethod(payment.type)
        val valor = payment.amount.toStringWithoutDots()
        val restricoes = mapRestriction(
            payment.type,
            payment.installmentDetails?.installments ?: 1
        )

        Log.d("PaymentProcessor", payment.toString())

        val i = Intent("br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Parâmetros de entrada para o SiTef
            putExtra("empresaSitef", "00000000")
            putExtra("enderecoSitef", "127.0.0.1;127.0.0.1:20036")
            putExtra("operador", "0001")
            putExtra("CNPJ_CPF", "12345678912345")

            // Dados da transação
            putExtra("data", getCurrentDate())
            putExtra("hora", getCurrentTime())
            putExtra("numeroCupom", getCurrentDate() + getCurrentTime())

            putExtra("valor", valor)
            putExtra("modalidade", modalidade)

            // Verifica se há parcelamento
            if (payment.installmentDetails != null) {
                putExtra("numParcelas", payment.installmentDetails?.installments.toString())
            }

            // Confiugurações adicionais
            putExtra("acessibilidadeVisual", "0")
            putExtra("timeoutColeta", "60")

            putExtra("restricoes", restricoes)

            Log.d("PaymentProcessor", "Intent extras: ${this.extras}")
        }
        context.startActivity(i)
    }

    /** Mapeia o tipo de pagamento para o código esperado pelo SiTef */
    private fun mapPaymentMethod(method: PaymentType): String {
        return when (method) {
            PaymentType.DEBIT -> "2"
            PaymentType.CREDIT -> "3"
            PaymentType.PIX -> "122"
            else -> "0"
        }
    }

    /** Mapeia as restrições de transação com base no tipo de pagamento e parcelamento */
    private fun mapRestriction(method: PaymentType, installment: Int): String {
        val restrictionCode = when (method) {
            PaymentType.DEBIT -> "16"
            PaymentType.CREDIT -> {
                if (installment > 1) "27" else "26"
            }

            else -> "0"
        }
        return "TransacoesHabilitadas=$restrictionCode"
    }
}