package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;

import java.util.function.Consumer;

public class ButtonPlay extends ButtonBase {

    public enum State { Play, Pause, Stop }

    private State state;

    private Consumer<ButtonPlay> stateChangeListener;

    public ButtonPlay(Context context) {
        super(context);
        init();
    }

    public ButtonPlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonPlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        state = State.Stop;
        setImageResource(R.drawable.baseline_play_arrow_24);
        setOnClickListener(v -> onClick());
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;

        if (state == State.Stop) {
            setImageResource(R.drawable.baseline_play_arrow_24);
        }
        if (state == State.Play) {
            setImageResource(R.drawable.baseline_pause_24);
        }
        if (state == State.Pause) {
            setImageResource(R.drawable.baseline_play_arrow_24);
        }

        if (stateChangeListener != null) {
            stateChangeListener.accept(this);
        }
    }

    public void setStateChangeListener(Consumer<ButtonPlay> listener) {
        stateChangeListener = listener;
    }

    private void onClick() {
        State newState = null;

        if (state == State.Stop) {
            newState = State.Play;
        }
        if (state == State.Play) {
            newState = State.Pause;
        }
        if (state == State.Pause) {
            newState = State.Play;
        }

        setState(newState);
    }
}
