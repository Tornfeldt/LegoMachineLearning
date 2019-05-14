package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import es.jepp.legomachinelearning.R
import es.jepp.legomachinelearning.StaticSettings
import kotlinx.android.synthetic.main.activity_select_model_before_driving.*

class SelectModelBeforeDrivingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_model_before_driving)

        var modelNames = listTrainedModels()
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = SelectModelViewAdapter(this, modelNames)
        adapter.setClickListener(object: SelectModelViewAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int, modelName: String) {
                openDriveActivity(modelName)
            }
        })
        recyclerView.adapter = adapter
    }

    fun openDriveActivity(modelName: String) {
        val intent = Intent(this, DriveActivity::class.java)
        intent.putExtra("ModelName", modelName)
        this.startActivity(intent)

        this.finish()
    }

    fun listTrainedModels(): List<String> {
        val dataDirectory = getExternalFilesDir(StaticSettings.BASE_FOLDER_NAME)
        val allFiles = dataDirectory.listFiles()
        val modelNames = mutableListOf<String>()

        for (file in allFiles){
            if (file.isFile && file.name.endsWith(StaticSettings.TRAINED_MODEL_FILE_ENDING)){
                var modelName = file.name.removeSuffix(StaticSettings.TRAINED_MODEL_FILE_ENDING)
                modelNames.add(modelName)
            }
        }

        return modelNames
    }
}
