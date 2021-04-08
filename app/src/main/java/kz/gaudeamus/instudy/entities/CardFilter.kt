package kz.gaudeamus.instudy.entities

/**
 * Сущность фильтра для поиска карт.
 */
data class CardFilter(var city: String?, var faculty: String?, var speciality: String?, var title: String?, var count: Int = 50)