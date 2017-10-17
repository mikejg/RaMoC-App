package org.gareiss.mike.ramoc.tv;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.gareiss.mike.ramoc.R;
import org.gareiss.mike.ramoc.model.Programme;

import java.util.Comparator;
import java.util.List;

class ProgrammeListAdapter extends ArrayAdapter<Programme> {

    Activity context;
    List<Programme> list;

    ProgrammeListAdapter(Activity context, List<Programme> list) {
        super(context, R.layout.item_programm, list);
        this.context = context;
        this.list = list;
    }

    public void sort() {
        sort(new Comparator<Programme>() {

            public int compare(Programme x, Programme y) {
                return x.compareTo(y);
            }
        });
    }

    public void updateView(ListView listView, Programme programme) {
        for (int i = 0; i < listView.getChildCount(); i++) {
            View view = listView.getChildAt(i);
            int pos = listView.getPositionForView(view);
            Programme pr = (Programme) listView.getItemAtPosition(pos);

            if (view.getTag() == null || pr == null) {
                continue;
            }

            if (programme.id != pr.id) {
                continue;
            }

            ProgrammListViewWarpper wrapper = (ProgrammListViewWarpper) view.getTag();
            wrapper.repaint(programme);
            break;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ProgrammListViewWarpper wrapper = null;

        if (row == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate(R.layout.item_programm, null, false);

            wrapper = new ProgrammListViewWarpper(row);
            row.setTag(wrapper);

        } else {
            wrapper = (ProgrammListViewWarpper) row.getTag();
        }

        Programme p = getItem(position);
        wrapper.repaint(p);
        return row;
    }
}

