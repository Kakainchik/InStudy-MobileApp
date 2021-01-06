package kz.gaudeamus.instudy.models

data class Resource<D>(val status: Status, val data: D?, val error: String?)

enum class Status {
	PROCESING,
	COMPLETED,
	CANCELED
}