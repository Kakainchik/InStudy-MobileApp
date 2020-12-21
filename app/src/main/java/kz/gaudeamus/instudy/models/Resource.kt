package kz.gaudeamus.instudy.models

data class Resource<D>(val status: Status, val data: D?, val exception: Exception?)

enum class Status {
	PROCESING,
	COMPLETED,
	CANCELED
}