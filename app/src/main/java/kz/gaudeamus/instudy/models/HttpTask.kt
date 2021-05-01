package kz.gaudeamus.instudy.models

/**
 * Объект ассинхронной веб-задачи.
 */
data class HttpTask<D>(val taskStatus: TaskStatus, val data: D?, val webStatus: WebStatus) {

	/**
	 * Состояние веб-задачи.
	 */
	public enum class TaskStatus {
		PROCESSING,
		COMPLETED,
		CANCELED
	}

	/**
	 * Статус HTTP ответа.
	 */
	public enum class WebStatus {
		NONE,
		TIMEOUT,
		UNABLE_CONNECT,
		UNAUTHORIZED,
		METHOD_NOT_ALLOWED,
		UNPROCESSABLE_ENTITY
	}
}