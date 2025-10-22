package com.coxphysics.terrapins.models.frc

import java.util.stream.Stream
import kotlin.streams.asSequence

class Complex(real: Double, imag: Double)
{
    val real_ = real
    val imag_ = imag

    fun real(): Double
    {
        return real_
    }

    fun imag(): Double
    {
        return imag_
    }

    fun structurally_equal(other: Complex): Boolean
    {
        val value = real() == other.real() && imag() == other.imag()
        return value
    }

    fun norm_sqr(): Double
    {
        val real = real_ * real_
        val imag = imag_ * imag_
        return real + imag
    }

    fun conjugate(): Complex
    {
        return Complex(real_, -imag_)
    }

    fun mult(other: Complex): Complex
    {
        val real = real_mult(other)
        val imag = (real_ * other.imag_) + (imag_ * other.real_)
        return Complex(real, imag)
    }

    fun real_mult(other: Complex): Double
    {
        return (real_ * other.real_) - (imag_ * other.imag_)
    }

    fun real_conj_mult(other: Complex): Double
    {
        return (real_ * other.real_) + (imag_ * other.imag_)
    }
}

fun complex_correlation(values : Sequence<Pair<Complex, Complex>>): Double
{
    var numerator = 0.0
    var denominator_1 = 0.0
    var denominator_2 = 0.0
    for (value in values)
    {
        val num = value.first.real_conj_mult(value.second)
        numerator += num;

        val denom_1 = value.first.norm_sqr()
        val denom_2 = value.second.norm_sqr()
        denominator_1 += denom_1
        denominator_2 += denom_2
    }
    val denominator = Math.sqrt(denominator_1 * denominator_2)
    return numerator / denominator
}

fun complex_correlation_from_stream(values : Stream<Pair<Complex, Complex>>) : Double
{
    return complex_correlation(values.asSequence())
}