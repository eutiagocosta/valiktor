package org.valiktor

import kotlin.reflect.KProperty1

/**
 * Validate an object
 *
 * Represents the extension function for validating any Kotlin object
 * If any constraint is violated, a [ConstraintViolationException] will be thrown
 *
 * @param block specifies the function DSL
 * @receiver the object to be validated
 * @return the same receiver object
 * @throws ConstraintViolationException
 *
 * @author Rodolpho S. Couto
 * @since 0.1.0
 */
fun <E> E.validate(block: ValidatorDsl<E>.() -> Unit): E {
    val validator = ValidatorDsl(this).apply(block)
    if (validator.constraints.isNotEmpty()) {
        throw ConstraintViolationException(validator.constraints)
    }
    return this
}

/**
 * Represents the DSL class that contains extended validation functions
 *
 * @param obj specifies the object to be validated
 * @property constraints specifies the set of [ConstraintViolation]
 * @constructor creates new DSL object
 *
 * @author Rodolpho S. Couto
 * @see Constraint
 * @see ConstraintViolation
 * @since 0.1.0
 */
open class ValidatorDsl<E>(private val obj: E) {

    /**
     * Specifies the violated constraints
     */
    val constraints = mutableSetOf<ConstraintViolation>()

    /**
     * Validates the property initializing another DSL function recursively
     *
     * @param block specifies the function DSL
     * @receiver the property to be validated
     * @return the same receiver property
     */
    @JvmName("validate")
    fun <T> KProperty1<E, T>.validate(block: ValidatorDsl<T>.() -> Unit): KProperty1<E, T> {
        constraints += ValidatorDsl(this.get(obj)).apply(block).constraints.map {
            DefaultConstraintViolation(
                    property = "${this.name}.${it.property}",
                    value = this.get(obj) as Any,
                    constraint = it.constraint)
        }
        return this
    }

    /**
     * Validates the iterable property initializing another DSL function recursively
     *
     * @param block specifies the function DSL
     * @receiver the property to be validated
     * @return the same receiver property
     */
    @JvmName("validateIterable")
    fun <T> KProperty1<E, Iterable<T>>.validate(block: ValidatorDsl<T>.() -> Unit): KProperty1<E, Iterable<T>> {
        this.get(obj).forEachIndexed { index, value ->
            constraints += ValidatorDsl(value).apply(block).constraints.map {
                DefaultConstraintViolation(
                        property = "${this.name}[$index].${it.property}",
                        value = value as Any,
                        constraint = it.constraint)
            }
        }
        return this
    }

    /**
     * Validates the array property initializing another DSL function recursively
     *
     * @param block specifies the function DSL
     * @receiver the property to be validated
     * @return the same receiver property
     */
    @JvmName("validateArray")
    fun <T> KProperty1<E, Array<T>>.validate(block: ValidatorDsl<T>.() -> Unit): KProperty1<E, Array<T>> {
        this.get(obj).forEachIndexed { index, value ->
            constraints += ValidatorDsl(value).apply(block).constraints.map {
                DefaultConstraintViolation(
                        property = "${this.name}[$index].${it.property}",
                        value = value as Any,
                        constraint = it.constraint)
            }
        }
        return this
    }

    /**
     * Validates the property by passing the constraint and the validation function
     *
     * This function is private and is used by all constraint validations
     *
     * @param constraint specifies the constraint that will be validated
     * @param isValid specifies the validation function
     * @receiver the property to be validated
     * @return the same receiver property
     */
    @JvmName("validateProperty")
    private fun <T> KProperty1<E, T>.validate(constraint: Constraint, isValid: (T) -> Boolean): KProperty1<E, T> {
        val value = this.get(obj)
        if (constraints.none { it.property == this.name } && !isValid(value)) {
            constraints += DefaultConstraintViolation(this.name, value, constraint)
        }
        return this
    }

    /**
     * Validates if the property value is null
     *
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isNull(): KProperty1<E, T> =
            this.validate(Null(), { it == null })

    /**
     * Validates if the property value is not null
     *
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isNotNull(): KProperty1<E, T> =
            this.validate(NotNull(), { it != null })

    /**
     * Validates if the property value is equal to another value
     *
     * @param value specifies the value that should be equal
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isEqualTo(value: T): KProperty1<E, T> =
            this.validate(Equals(value), { it == value })

    /**
     * Validates if the property value isn't equal to another value
     *
     * @param value specifies the value that should not be equal
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isNotEqualTo(value: T): KProperty1<E, T> =
            this.validate(NotEquals(value), { it != value })

    /**
     * Validates if the property value is equal to one of the values
     *
     * @param values specifies the array of values to be compared
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isIn(vararg values: T): KProperty1<E, T> =
            this.validate(In(values.toSet()), { values.contains(it) })

    /**
     * Validates if the property value is equal to one of the values
     *
     * @param values specifies the iterable of values to be compared
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isIn(values: Iterable<T>): KProperty1<E, T> =
            this.validate(In(values), { values.contains(it) })

    /**
     * Validates if the property value isn't equal to any value
     *
     * @param values specifies the array of values to be compared
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isNotIn(vararg values: T): KProperty1<E, T> =
            this.validate(NotIn(values.toSet()), { !values.contains(it) })

    /**
     * Validates if the property value isn't equal to any value
     *
     * @param values specifies the iterable of values to be compared
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isNotIn(values: Iterable<T>): KProperty1<E, T> =
            this.validate(NotIn(values), { !values.contains(it) })

    /**
     * Validates if the property is valid by passing a custom function
     *
     * @param validator specifies the validation function
     * @receiver the property to be validated
     * @return the same receiver property
     */
    fun <T> KProperty1<E, T>.isValid(validator: (T) -> Boolean): KProperty1<E, T> =
            this.validate(Valid(validator), validator)
}