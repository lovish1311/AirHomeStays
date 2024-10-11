package com.airhomestays.app.ui.profile.about.why_Host

class ImageModel(var image_drawable: String = "", var text_string: String = "",var button: String = "") {

    fun getImage_drawables(): String {
        return image_drawable
    }

    fun setImage_drawables(image_drawable: String) {
        this.image_drawable = image_drawable
    }

    fun getText_srings(): String {
        return text_string
    }

    fun setText_srings(text_string: String) {
        this.text_string = text_string
    }

    fun getButton_srings(): String {
        return button
    }

    fun setButton_srings(text_string: String) {
        this.button = text_string
    }
}