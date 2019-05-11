package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import es.jepp.legomachinelearning.R
import es.jepp.legomachinelearning.StaticSettings
import kotlinx.android.synthetic.main.activity_save_model_name.*
import java.io.File

class SaveModelNameActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_model_name)

        val a = this

        saveButton.setOnClickListener {
            if (!isExternalStorageWritable()) {
                Toast.makeText(this, "Storage not mounted", Toast.LENGTH_LONG).show()
            }

            var dataDirectory = this.getExternalFilesDir(StaticSettings.BASE_FOLDER_NAME)

            val modelName = modelNameInput.text.toString()

            var fileCreated = false

            try {
                File(dataDirectory, modelName).createNewFile()
                fileCreated = true
            }
            catch (e: Exception){
                Toast.makeText(this, "Unable to create model file", Toast.LENGTH_LONG).show()
            }

            if (fileCreated) {
                val intent = Intent(this, CollectDataActivity::class.java)
                intent.putExtra("ModelName", modelName)
                this.startActivity(intent)

                this.finish()
            }
        }
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}
