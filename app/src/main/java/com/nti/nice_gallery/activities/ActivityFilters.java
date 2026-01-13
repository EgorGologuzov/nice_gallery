package com.nti.nice_gallery.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.data.ManagerOfSettings_Test1;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.ReadOnlyList;

import com.nti.nice_gallery.models.ModelFileFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityFilters extends AppCompatActivity {
    private ChipGroup extensions;
    private ChipGroup types;
    EditText minWeight;
    EditText maxWeight;
    Button minCreateDate;
    Button maxCreateDate;
    Button minUpdateDate;
    Button maxUpdateDate;
    DateTimeFormatter dateFormatter;
    Button minDuration,maxDuration;
    Switch ignoreHidden;
    int hour,minute;
    IManagerOfSettings managerOfSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        Log.d("dbg","Created");

        dateFormatter=DateTimeFormatter.ofPattern(getString(R.string.format_java_simple_date_full_numeric));
        setupComponents();
        fillInExtensions();
        fillInFromSave();

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            saveFilters();
        });

        Button resetButton=findViewById(R.id.resetButton);
        resetButton.setOnClickListener(view -> {
            managerOfSettings.saveFilters(null);
            Toast.makeText(this,"Настройки успешно сброшены",Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        Log.d("dbg","Destroyed");
        super.onDestroy();
    }

    private void saveFilters() {
        types = findViewById(R.id.types);
        List<ModelMediaFile.Type> resultTypes=null;
        if (!types.getCheckedChipIds().isEmpty()){
            resultTypes = new ArrayList<>();
            for (int itemId : types.getCheckedChipIds()) {
                Chip chip = findViewById(itemId);
                ModelMediaFile.Type resultType = ModelMediaFile.Type.valueOf(chip.getText().toString());
                resultTypes.add(resultType);
            }
        }

        extensions = findViewById(R.id.extensions);
        List<String> resultExtensions = new ArrayList<>();
        if (!extensions.getCheckedChipIds().isEmpty()){
            resultExtensions = new ArrayList<>();
            for (int itemId : extensions.getCheckedChipIds()) {
                Chip chip = findViewById(itemId);
                String resultExtension = chip.getText().toString();
                resultExtensions.add(resultExtension);
            }
        }

        minWeight = findViewById(R.id.minWeight);
        Long minWeightValue = null;
        if (!minWeight.getText().toString().isEmpty()) {
            minWeightValue = Long.parseLong(minWeight.getText().toString());
        }

        maxWeight = findViewById(R.id.maxWeight);
        Long maxWeightValue = null;
        if (!maxWeight.getText().toString().isEmpty()) {
            maxWeightValue = Long.parseLong(maxWeight.getText().toString());
        }

        ignoreHidden=findViewById(R.id.ignoreHidden);
        boolean ignoreHiddenValue=false;
        ignoreHiddenValue=ignoreHidden.isChecked();

        LocalDateTime minCreateAtValue = null;
        if (!minCreateDate.getText().toString().equals("Начальная дата")) {
            minCreateAtValue = LocalDate.parse(minCreateDate.getText().toString(), dateFormatter).atStartOfDay();
        }

        LocalDateTime maxCreateAtValue = null;
        if (!maxCreateDate.getText().toString().equals("Конечная дата")) {
            maxCreateAtValue = LocalDate.parse(maxCreateDate.getText().toString(), dateFormatter).atTime(23, 59, 59);
        }

        LocalDateTime minUpdateAtValue=null;
        if (!minUpdateDate.getText().toString().equals("Начальная дата")){
            minUpdateAtValue = LocalDate.parse(minUpdateDate.getText().toString(), dateFormatter).atTime(23, 59, 59);
        }

        LocalDateTime maxUpdateAtValue = null;
        if (!maxUpdateDate.getText().toString().equals("Конечная дата")) {
            maxUpdateAtValue = LocalDate.parse(maxUpdateDate.getText().toString(), dateFormatter).atTime(23, 59, 59);
        }

        maxDuration=findViewById(R.id.maxDuration);
        minDuration=findViewById(R.id.minDuration);

        Integer maxDurationValue=null;
        if (!maxDuration.getText().toString().equals("конечная")){
            maxDurationValue=Integer.parseInt(maxDuration.getText().toString().split(":")[0])*60+
                    Integer.parseInt(maxDuration.getText().toString().split(":")[1]);
        }

        Integer minDurationValue=null;
        if (!minDuration.getText().toString().equals("начальная")){
            minDurationValue=Integer.parseInt(minDuration.getText().toString().split(":")[0])*60+
                    Integer.parseInt(minDuration.getText().toString().split(":")[1]);
        }

        ModelFilters resultModel = new ModelFilters(
                ignoreHiddenValue,
                new ReadOnlyList<>(resultTypes),
                minWeightValue,
                maxWeightValue,
                minCreateAtValue,
                maxCreateAtValue,
                minUpdateAtValue,
                maxUpdateAtValue,
                new ReadOnlyList<>(resultExtensions),
                minDurationValue,
                maxDurationValue
        );

        managerOfSettings.saveFilters(resultModel);
        Toast.makeText(this, "Настройки успешно сохранены", Toast.LENGTH_SHORT).show();

        Intent toBintent = new Intent(this, ActivityMain.class);
        toBintent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(toBintent);
    }

    private void fillInFromSave() {
        ModelFilters filterModel=managerOfSettings.getFilters();

        if (filterModel==null)
            return;

        if (filterModel.minWeight != null)
            minWeight.setText(String.valueOf(filterModel.minWeight));
        if (filterModel.maxWeight != null)
            maxWeight.setText(String.valueOf(filterModel.maxWeight));
        if (filterModel.maxCreateAt != null)
            maxCreateDate.setText(String.valueOf(filterModel.maxCreateAt));
        if (filterModel.minCreateAt != null)
            minCreateDate.setText(String.valueOf(filterModel.minCreateAt));
        if (filterModel.minUpdateAt != null)
            minUpdateDate.setText(String.valueOf(filterModel.minUpdateAt));
        if (filterModel.maxUpdateAt != null)
            maxUpdateDate.setText(String.valueOf(filterModel.maxUpdateAt));
        if (filterModel.minDuration != null)
            minDuration.setText(String.valueOf(filterModel.minDuration));
        if (filterModel.maxDuration != null)
            maxDuration.setText(String.valueOf(filterModel.maxDuration));

        if(filterModel.types != null){
            for (int i=0; i<types.getChildCount();i++){
                View child = types.getChildAt(i);
                if (child instanceof Chip){
                    Chip chip = (Chip) child;

                    if (filterModel.types.contains(ModelMediaFile.Type.valueOf(chip.getText().toString())))
                        chip.setChecked(true);
                }
            }
        }

        if(filterModel.extensions != null){
            Log.d("Ext","1");
            for (int i = 0; i < extensions.getChildCount(); i++) {
                View child = extensions.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;

                    if (filterModel.extensions.contains(chip.getText().toString())){
                        Log.d("Ext",chip.getText().toString());
                        chip.setChecked(true);
                    }
                }
            }
        }
    }

    private void fillInExtensions() {
        extensions = findViewById(R.id.extensions);
        ModelFileFormat[] extensionList = ModelMediaFile.supportedMediaFormats;
        for(ModelFileFormat file:extensionList){
            Chip chip=new Chip(this);
            chip.setText(file.fileExtension);
            chip.setCheckable(true);
            extensions.addView(chip);
        }
    }

    private void setupComponents() {
        minCreateDate=findViewById(R.id.minCreateDate);
        maxCreateDate=findViewById(R.id.maxCreateDate);
        minCreateDate.setOnClickListener(view1 -> {
            DatePickerDialog dialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    LocalDate date=LocalDate.of(year,month+1,day);
                    minCreateDate.setText(date.format(dateFormatter));
                };
            },2025,11,25);
            dialog.show();
        });
        maxCreateDate.setOnClickListener(view1 -> {
            DatePickerDialog dialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    LocalDate date=LocalDate.of(year,month+1,day);
                    maxCreateDate.setText(date.format(dateFormatter));
                };
            },2025,11,25);
            dialog.show();
        });

        minUpdateDate=findViewById(R.id.minUpdateDate);
        maxUpdateDate=findViewById(R.id.maxUpdateDate);
        minUpdateDate.setOnClickListener(view1 -> {
            DatePickerDialog dialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    LocalDate date=LocalDate.of(year,month+1,day);
                    minUpdateDate.setText(date.format(dateFormatter));
                };
            },2025,11,25);
            dialog.show();
        });
        maxUpdateDate.setOnClickListener(view1 -> {
            DatePickerDialog dialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    LocalDate date=LocalDate.of(year,month+1,day);
                    maxUpdateDate.setText(date.format(dateFormatter));
                };
            },2025,11,25);
            dialog.show();
        });

        minDuration=findViewById(R.id.minDuration);
        minDuration.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener onTimeSetListener=new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    hour=selectedHour;
                    minute=selectedMinute;
                    minDuration.setText(String.format(Locale.getDefault(),getString(R.string.format_duration_short),hour,minute));
                }
            };
            TimePickerDialog dialog=new TimePickerDialog(this,onTimeSetListener,hour,minute,true);
            dialog.show();
        });
        maxDuration=findViewById(R.id.maxDuration);
        maxDuration.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener onTimeSetListener=new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    hour=selectedHour;
                    minute=selectedMinute;
                    maxDuration.setText(String.format(Locale.getDefault(),getString(R.string.format_duration_short),hour,minute));
                }
            };
            TimePickerDialog dialog=new TimePickerDialog(this,onTimeSetListener,hour,minute,true);
            dialog.show();
        });

        managerOfSettings=new ManagerOfSettings_Test1(this);
    }
}
