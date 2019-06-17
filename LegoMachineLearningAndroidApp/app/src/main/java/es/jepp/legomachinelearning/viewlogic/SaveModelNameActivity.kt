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

            val modelName = modelNameInput.text.toString()

            if (modelName == null || modelName == "") {
                Toast.makeText(this, "Specify a model name", Toast.LENGTH_LONG).show()
            } else if (getDataFile(modelName).exists()) {
                Toast.makeText(this, "Model already exists", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, CollectDataActivity::class.java)
                intent.putExtra("ModelName", modelName)
                this.startActivity(intent)

                this.finish()
            }
        }
    }

    private fun getDataFile(modelName: String): File {
        val dataDirectory = getExternalFilesDir(StaticSettings.BASE_FOLDER_NAME)
        val fileName = modelName + StaticSettings.DATA_FILE_ENDING
        return File(dataDirectory, fileName)
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}
