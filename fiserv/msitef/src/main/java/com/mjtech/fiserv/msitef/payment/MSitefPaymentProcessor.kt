package com.mjtech.fiserv.msitef.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mjtech.fiserv.msitef.common.ACESSIBILIDADE_VISUAL
import com.mjtech.fiserv.msitef.common.CD_HABILITADAS
import com.mjtech.fiserv.msitef.common.CNPJ_CPF
import com.mjtech.fiserv.msitef.common.EMPRESA_SITEF
import com.mjtech.fiserv.msitef.common.ENDERECO_SITEF
import com.mjtech.fiserv.msitef.common.OPERADOR
import com.mjtech.fiserv.msitef.common.TIMEOUT_COLETA
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

        Log.d(TAG, payment.toString())

        val fiservIntent = Intent("br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF").apply {

            // Parâmetros de entrada para o SiTef
            putExtra("empresaSitef", EMPRESA_SITEF)
            putExtra("enderecoSitef", ENDERECO_SITEF)
            putExtra("operador", OPERADOR)
            putExtra("CNPJ_CPF", CNPJ_CPF)

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

            // Parâmetros específicos para o PIX
            if (payment.type == PaymentType.PIX) {
                putExtra("transacoesHabilitadas", "7;8;")
                putExtra("cnpj_automacao", CNPJ_CPF)
            }

            // Confiugurações adicionais
            putExtra("acessibilidadeVisual", ACESSIBILIDADE_VISUAL)
            putExtra("timeoutColeta", TIMEOUT_COLETA)

            putExtra("restricoes", restricoes)

            Log.d(TAG, "Intent extras: ${this.extras}")
        }

        MSitefPaymentHolder.initialize(callback, payment)

        val paymentIntent = Intent(context, MSitefPaymentActivity::class.java).apply {
            putExtra(MSitefPaymentActivity.FISERV_INTENT_KEY, fiservIntent)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        context.startActivity(paymentIntent)
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
        val restrictionType =
            if (method == PaymentType.PIX) "CarteirasDigitaisHabilitadas"
            else "TransacoesHabilitadas"
        val restrictionCode = when (method) {
            PaymentType.DEBIT -> "16"
            PaymentType.CREDIT -> {
                if (installment > 1) "27" else "26"
            }

            PaymentType.PIX -> CD_HABILITADAS
            else -> "0"
        }
        return "$restrictionType=$restrictionCode"
    }

    companion object {
        const val TAG = "MSitefPaymentProcessor"
    }
}