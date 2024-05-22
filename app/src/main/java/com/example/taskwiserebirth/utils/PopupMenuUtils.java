package com.example.taskwiserebirth.utils;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.task.TaskAdapter;

public class PopupMenuUtils {

    public static void showPopupMenu(Context context, View view, Task task, TaskAdapter.TaskActionListener actionListener, FragmentActivity activity) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.show_menu, popupMenu.getMenu());

        // Adjust layout based on navigation bar visibility
        SystemUIHelper.setFlagsOnThePeekView();

        Menu menu = popupMenu.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpannableString spannable = new SpannableString(menuItem.getTitle());
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark)), 0, spannable.length(), 0);
            menuItem.setTitle(spannable);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            SpannableString selectedSpannable = new SpannableString(item.getTitle());
            selectedSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), 0, selectedSpannable.length(), 0);
            item.setTitle(selectedSpannable);

            int itemId = item.getItemId();
            if (itemId == R.id.menuEdit) {
                actionListener.onEditTask(task);
                return true;
            } else if (itemId == R.id.menuDelete) {
                actionListener.onDeleteTask(task);
                return true;
            } else if (itemId == R.id.menuDone) {
                actionListener.onDoneTask(task);
                return true;
            }
            return false;
        });

        popupMenu.setOnDismissListener(dialogInterface -> {
            if (activity != null) {
                SystemUIHelper.setSystemUIVisibility((AppCompatActivity) activity);
            }
        });

        popupMenu.show();
    }
}
