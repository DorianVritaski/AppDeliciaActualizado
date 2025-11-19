package com.example.appdelicia01.controllers;

// Importa tu modelo de Product para que la interfaz sepa de qué tipo de objeto habla
import com.example.appdelicia01.models.Product;

/**
 * Esta interfaz define el "contrato" entre EditProductActivity (la Vista)
 * y EditProductController (el Controlador).
 */
public interface EditProductView {

    /**
     * Muestra u oculta la barra de progreso.
     * @param isLoading true para mostrar, false para ocultar.
     */
    void showLoading(boolean isLoading);

    /**
     * Muestra un mensaje de error al usuario.
     * @param message El mensaje de error a mostrar.
     */
    void displayError(String message);

    /**
     * Rellena los campos de la pantalla con los datos de un producto existente.
     * @param product El objeto Product con los datos a mostrar.
     */
    void displayProductDetails(Product product);

    /**
     * Se llama cuando los cambios se han guardado con éxito en la base de datos.
     * La vista normalmente mostrará un mensaje y se cerrará.
     */
    void onSaveChangesSuccess();
}
