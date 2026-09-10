package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelProgress;
import com.nti.nice_gallery.utils.ManagerOfBackground;
import com.nti.nice_gallery.utils.ManagerOfNotifications;

public class ViewForDeveloper extends LinearLayout {

    public ViewForDeveloper(Context context) {
        super(context);
        init();
    }

    public ViewForDeveloper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewForDeveloper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_for_developer, this);

        ManagerOfNotifications managerOfNotifications = new ManagerOfNotifications(getContext());
        ManagerOfBackground managerOfBackground = new ManagerOfBackground(getContext());

        Button buttonTest1 = findViewById(R.id.buttonTest1);
        Button buttonTest2 = findViewById(R.id.buttonTest2);
        Button buttonTest3 = findViewById(R.id.buttonTest3);

        buttonTest1.setOnClickListener(btn -> {
            ManagerOfBackground.BackgroundTask timerTask = new ManagerOfBackground.BackgroundTask() {
                private final int totalSeconds = 5;

                @Override
                public void start() {
                    try {
                        for (int i = 0; i <= totalSeconds; i++) {
                            // Проверяем, не была ли вызвана отмена
                            if (isCancelled()) {
                                notifyCanceled("Timer 1 canceled.");
                                return;
                            }

                            // Создаем объект прогресса
                            ModelProgress progress = new ModelProgress(i, totalSeconds, "Timer 1");

                            // Отправляем прогресс в уведомление
                            notifyProgress(progress);

                            // Ждем 1 секунду
                            Thread.sleep(1000);
                        }

                        // Таймер завершен
                        notifyFinished("Timer 1 finished.");

                    } catch (InterruptedException e) {
                        // Поток был прерван
                        notifyCanceled("Timer 1 interrupted.");
                    }
                }
            };

            managerOfBackground.startTask(timerTask);
        });

        buttonTest2.setOnClickListener(btn -> {
            ManagerOfBackground.BackgroundTask timerTask = new ManagerOfBackground.BackgroundTask() {
                private final int totalSeconds = 6;

                @Override
                public void start() {
                    try {
                        for (int i = 0; i <= totalSeconds; i++) {
                            // Проверяем, не была ли вызвана отмена
                            if (isCancelled()) {
                                notifyCanceled("Timer 2 canceled");
                                return;
                            }

                            // Создаем объект прогресса
                            ModelProgress progress = new ModelProgress(i, totalSeconds, "Timer 2");

                            // Отправляем прогресс в уведомление
                            notifyProgress(progress);

                            // Ждем 1 секунду
                            Thread.sleep(1000);
                        }

                        // Таймер завершен
                        notifyFinished("Timer 2 finished.");

                    } catch (InterruptedException e) {
                        // Поток был прерван
                        notifyCanceled("Timer 2 interrupted.");
                    }
                }
            };

            managerOfBackground.startTask(timerTask);
        });

        buttonTest3.setOnClickListener(btn -> {
            managerOfNotifications.showToast("Test 3. No action");
        });
    }
}
