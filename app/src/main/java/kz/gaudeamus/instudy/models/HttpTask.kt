package kz.gaudeamus.instudy.models

data class HttpTask<D>(val taskStatus: TaskStatus, val data: D?, val webStatus: WebStatus) {

	public enum class TaskStatus {
		PROCESSING,
		COMPLETED,
		CANCELED
	}

	public enum class WebStatus {
		NONE,
		TIMEOUT,
		UNABLE_CONNECT,
		UNAUTHORIZED
	}
}