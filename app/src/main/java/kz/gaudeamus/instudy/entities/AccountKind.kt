package kz.gaudeamus.instudy.entities

/**
 * Типы аккаунтов, хранящиеся в базе.
 * Для POST-запросов используются целые числа. Во избежание ошибок, [enum] на клиенте должен иметь такие же коды к каждому типу.
 * В самой базе данныx <!--В данный момент используется PostgreSQL--> типы описываются такими же кодами.
 */
enum class AccountKind(val value: Int) {
    STUDENT(31),
    SCHOOL(50),
    MODERATOR(42);

    companion object {
        fun from(findValue: Int): AccountKind = values().first { it.value == findValue }
    }
}