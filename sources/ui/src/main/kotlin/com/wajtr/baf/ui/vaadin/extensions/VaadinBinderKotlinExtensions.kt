package com.wajtr.baf.ui.vaadin.extensions

import com.vaadin.flow.component.HasValue
import com.vaadin.flow.data.binder.Binder
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * Extended Binder class to work better with Kotlin var-properties (mutable). Using this function you can bind Vaadin component directly to the Kotlin var property.
 * Based on https://dzone.com/articles/kotlin-properties-and-the-vaadin-8-beanbinder
 *
 * @author Bretislav Wajtr
 */
fun <BEAN, T> Binder.BindingBuilder<BEAN, T>.bindMutableProperty(prop: KMutableProperty<T>): Binder.Binding<BEAN, T> {
    return this.bind(
        { bean: BEAN -> prop.getter.call(bean) },
        { bean: BEAN, v: T -> prop.setter.call(bean, v) })
}

/**
 * Extended Binder class to work better with Kotlin val-properties (immutable). Using this function you can bind Vaadin component directly to the Kotlin val property.
 * Of course, due to the nature of val properties, setter of this binding does nothing
 * Based on https://dzone.com/articles/kotlin-properties-and-the-vaadin-8-beanbinder
 *
 * @author Bretislav Wajtr
 */
fun <BEAN, T> Binder.BindingBuilder<BEAN, T>.bindReadOnlyProperty(prop: KProperty<T>): Binder.Binding<BEAN, T> {
    return this.bind(
        { bean: BEAN -> prop.getter.call(bean) },
        { _: BEAN, _: T -> })
}


/**
 * Function type to be used in Presenters to allow Controllers binding to field components to bind using Binders.
 * This helps to hide implementation of the component which the Controller tries to bind to.
 */
typealias BindOperation<E, T> = (HasValue<E, T>) -> Unit