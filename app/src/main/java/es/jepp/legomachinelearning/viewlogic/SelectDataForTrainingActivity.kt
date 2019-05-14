package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import es.jepp.legomachinelearning.R
import es.jepp.legomachinelearning.StaticSettings
import kotlinx.android.synthetic.main.activity_select_data_for_training.*
import java.io.File

class SelectDataForTrainingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_data_for_training)

        var modelNames = listModelsWithData()
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = SelectModelViewAdapter(this, modelNames)
        adapter.setClickListener(object: SelectModelViewAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int, modelName: String) {
                openTrainActivity(modelName)
            }
        })
        recyclerView.adapter = adapter
    }

    fun openTrainActivity(modelName: String) {
        val intent = Intent(this, TrainActivity::class.java)
        intent.putExtra("ModelName", modelName)
        this.startActivity(intent)

        this.finish()
    }

    fun listModelsWithData(): List<String> {
        val dataDirectory = getExternalFilesDir(StaticSettings.BASE_FOLDER_NAME)
        val allFiles = dataDirectory.listFiles()
        val modelNames = mutableListOf<String>()

        for (file in allFiles){
            if (file.isFile && file.name.endsWith(StaticSettings.DATA_FILE_ENDING)){
                var modelName = file.name.removeSuffix(StaticSettings.DATA_FILE_ENDING)
                modelNames.add(modelName)
            }
        }

        return modelNames
    }
}
