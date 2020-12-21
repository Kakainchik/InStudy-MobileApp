package kz.gaudeamus.instudy

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import kz.gaudeamus.instudy.entities.RegistrationSchool
import kz.gaudeamus.instudy.models.RegistrationSchoolViewModel
import kz.gaudeamus.instudy.models.Status
import java.io.File
import java.io.FileInputStream

class SignUpSchoolFragment : Fragment() {

    private val CHOOSE_FILE_REQUESTCODE: Int = 100
    private val requiredFiles: HashMap<String, Uri> = HashMap()
    private val model: RegistrationSchoolViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_school, container, false)

        //Визуальные компоненты
        val signUpButton: MaterialButton = view.findViewById(R.id.signup_school_register_button)
        val addFileButton: MaterialButton = view.findViewById(R.id.signup_school_addfile_button)
        val emailText: TextInputEditText = view.findViewById(R.id.signup_school_email_text)
        val emailLayout: TextInputLayout = view.findViewById(R.id.signup_school_email_input)
        val passwordText: TextInputEditText = view.findViewById(R.id.signup_school_password_text)
        val passwordLayout: TextInputLayout = view.findViewById(R.id.signup_school_password_input)
        val repassText: TextInputEditText = view.findViewById(R.id.signup_school_repassword_text)
        val repassLayout: TextInputLayout = view.findViewById(R.id.signup_school_repassword_input)
        val organizationText: TextInputEditText = view.findViewById(R.id.signup_school_name_text)
        val organizationLayout: TextInputLayout = view.findViewById(R.id.signup_school_name_input)
        val noticeText: MaterialTextView = view.findViewById(R.id.signup_school_notice)

        //Переменные
        var isValid: Boolean = true

        //Нажимаем на кнопку регистрации
        signUpButton.setOnClickListener {
            //Верный ли адрес
            emailText.takeUnless {
                android.util.Patterns.EMAIL_ADDRESS.matcher(it.text.toString()).matches()
            }?.run {
                emailLayout.error = view.resources.getString(R.string.error_invalid_email)
                isValid = false
            } ?:run { emailLayout.error = null }

            //Верный ли пароль
            passwordText.takeUnless { isPasswordValid(it.text.toString()) }
                ?.run {
                    passwordLayout.error = view.resources.getString(R.string.error_unmatched_password)
                    isValid = false
                } ?:run { passwordLayout.error = null }

            //Верен ли повторный пароль
            repassText.takeUnless { it.text.toString().equals(passwordText.text.toString()) }
                ?.run {
                    repassLayout.error = view.resources.getString(R.string.error_invalid_repassword)
                    isValid = false
                } ?:run { repassLayout.error = null }

            //Не пустое ли имя
            organizationText.takeUnless { isNameOrganizationValid(it.text.toString().trim()) }
                ?.run {
                    organizationLayout.error = view.resources.getString(R.string.error_counter_minimum)
                    isValid = false
                }

            //Прикреплён ли хоть один документ
            requiredFiles.takeIf { it.isEmpty() }
                ?.run {
                    addFileButton.apply {
                        setTextColor(resources.getColor(R.color.colorAccent))
                        setIconTintResource(R.color.colorAccent)
                    }
                    isValid = false
                } ?:run {
                    addFileButton.apply {
                        setTextColor(resources.getColor(R.color.colorPrimary))
                        setIconTintResource(R.color.colorPrimary)
                    }
            }

            //Поля заполнены верно - запускаем процесс регистрации
            if(isValid) {
                //Заполняем массив реквезитами и кодируем в Base64
                var props = arrayOf<String>()
                requiredFiles.forEach { key, value ->
                    (context?.contentResolver?.openInputStream(value) as? FileInputStream).use {
                        props.plusElement(Base64.encodeToString(it?.readBytes(), Base64.NO_WRAP))
                    }
                }

                val data = RegistrationSchool(email = emailText.text.toString(),
                                              password = passwordText.text.toString(),
                                              organization = organizationText.text.toString(),
                                              props = props)
                model.registrate(data)
                /*while(model.registrationLiveData.value?.status == Status.PROCESING) {

                }*/
            }

            isValid = true
        }

        //Нажимаем на кнопку добавления файла
        addFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                setType("application/*")
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            startActivityForResult(intent, CHOOSE_FILE_REQUESTCODE)
        }

        //Выводим ошибку если сверхлимит длины
        organizationText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //NOTHING
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                organizationLayout.takeIf { s?.toString()?.length!! > it.counterMaxLength }
                    ?.run {
                        this.error = view.resources.getString(R.string.error_counter_limit)
                        isValid = false
                    } ?:run { organizationLayout.error = null }
            }

            override fun afterTextChanged(s: Editable?) {
                //Если ошибки нет
                organizationLayout.error?:run { isValid = true }
            }
        })

        //Устанавливаем предупреждение с html разметкой.
        noticeText.setText(Html.fromHtml(view.resources.getString(R.string.notice_school_registration_assert)))

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            CHOOSE_FILE_REQUESTCODE -> { //Выбор файла
                if(resultCode == RESULT_OK) data?.data?.let { this.importFile(it) }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //Добавляем chip с выбранным файлом
    private fun importFile(uri: Uri) {
        val file = File(uri.path)
        val addFileButton: MaterialButton? = view?.findViewById(R.id.signup_school_addfile_button)
        val chipGroup: ChipGroup? = view?.findViewById(R.id.signup_school_chipgroup)
        val chip = (layoutInflater.inflate(R.layout.single_chip_layout,
                                          chipGroup,
                                          false) as Chip).apply {
            setText(file.name)
            //Нажимаем на кнопку закрытия чипа
            setOnCloseIconClickListener {
                //Удаляем файл
                this@SignUpSchoolFragment.requiredFiles.remove(file.name)
                chipGroup?.removeView(it)
                //Если список опустел - возвращаем текст на кнопку
                if(this@SignUpSchoolFragment.requiredFiles.isEmpty())
                    addFileButton?.text = resources.getString(R.string.bt_add_file)
            }
        }

        //Если такого файла нет - добавляем, иначе - выводим ошибку
        chipGroup.takeUnless { this.requiredFiles.containsKey(file.name)}
            ?.run {
                //Добавляем в общий список
                this@SignUpSchoolFragment.requiredFiles.put(file.name, uri)
                this.addView(chip)
                //Убираем текст с кнопки, дабы не загораживал
                addFileButton?.text = null
            } ?:run {
                Toast.makeText(this@SignUpSchoolFragment.context,
                               R.string.error_same_file_exists,
                               Toast.LENGTH_LONG).show()
            }
    }

    /**
     * <p>  Current pattern: minimum 3 character,
     *      should contain at least 1 letter </p>
     * @param name should be trimmed
     * @return `true` if current name matches the matcher's pattern
     */
    private fun isNameOrganizationValid(name: String?): Boolean {
        name?.let {
            val namePattern = """^(?=.+\D).{3,}$"""
            val nameMatcher = Regex(namePattern)

            return nameMatcher.find(name) != null
        } ?: return false
    }
}