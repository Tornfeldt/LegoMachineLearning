package es.jepp.legomachinelearning

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_save_train_name.*
import java.io.File

class SaveTrainNameActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_train_name)

        val a = this

        saveButton.setOnClickListener {
            var dataDirectory = Environment.getDataDirectory()

            val modelName = modelNameInput.text

            var modelDirectory = File(dataDirectory, StaticSettings.BASE_FOLDER_NAME + modelNameInput.text)
            modelDirectory.mkdir()

            val intent = Intent(this, TrainActivity::class.java)
            intent.putExtra("ModelName", modelName)
            this.startActivity(intent)

            this.finish()
        }
    }
}
