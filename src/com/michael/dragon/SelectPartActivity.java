package com.michael.dragon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.michael.dragon.db.DgDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author M.TOYOTA 13/09/13 Created.
 * @author Copyright (C) 2013 MetaMoJi Corp. All Rights Reserved.
 */
public class SelectPartActivity extends Activity {
    long mBid = 0;
    public static final String PARAM_BID = PracticeActivity.PARAM_BID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DgDatabase.initialize(this);

        setContentView(R.layout.activity_select_part);
        getActionBar().setHomeButtonEnabled(true);

        Intent intent = getIntent();
        mBid = intent.getLongExtra(PARAM_BID, -1);
        if( mBid == -1 ) {
            finish();
            return;
        }

        refreshParts();

        ((ListView)findViewById(R.id.part_list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PartInfo info = (PartInfo)parent.getItemAtPosition(position);
                openPart(info.start);
            }
        });

        findViewById(R.id.random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPart(-1);
            }
        });

        findViewById(R.id.notes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewNotes();
            }
        });
    }

    private void viewNotes() {
        long notes[] = DgDatabase.getInstance().getAllNotes();
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(NoteActivity.PARAM_NOTE_IDS, notes);
        intent.putExtra(NoteActivity.PARAM_BID, mBid);
        intent.putExtra(NoteActivity.PARAM_ALL_NOTES, true);
        startActivity(intent);
    }

    /**
     */
    @Override
    protected void onPause() {
        super.onPause();
        DgDatabase db = DgDatabase.getInstance();
        db.touchBook(mBid);
    }

    /**
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshParts();
    }

    void refreshParts() {
        DgDatabase db = DgDatabase.getInstance();
        long[] qids = db.getQidList(mBid);
        int count = qids.length;
        int partCount = PracticeActivity.getGroupCount(count);

        ArrayList<PartInfo> ary = new ArrayList<PartInfo>(partCount);
        DgDatabase.RecordStatistics ss = new DgDatabase.RecordStatistics();
        for( int i=0 ; i<partCount ; i++ ) {
            int start = i * PracticeActivity.N_UNIT_COUNT_IN_GROUP;
            int end = (i+1) * PracticeActivity.N_UNIT_COUNT_IN_GROUP -1;
            if( i == partCount-1){
                end = count-1;
            }

            db.getRecordStatistics(qids[start], qids[end], false, ss);

            PartInfo info = new PartInfo();
            info.start = i;
            info.subject = Integer.toString(start+1) + " - " + Integer.toString(end+1);
            info.recent = Integer.toString(ss.recent) + "/" + Integer.toString(end-start+1);
            int ave_count = Math.round((float)(ss.ok+ss.ng)/(float)(end-start+1));
            info.ave_count = Integer.toString(ave_count);
            ary.add(info);
        }
        PartListAdapter adapter = new PartListAdapter(this, ary);
        ((ListView)findViewById(R.id.part_list)).setAdapter(adapter);
    }


    void openPart(int start){
        Intent intent = new Intent(this, PracticeActivity.class);
        intent.putExtra(PracticeActivity.PARAM_BID, mBid);
        intent.putExtra(PracticeActivity.PARAM_PART, start);
        startActivity(intent);
    }

    class PartInfo {
        public int start;
        String subject;
        String recent;
        String ave_count;
    }

    class PartListAdapter extends ArrayAdapter<PartInfo> {
        LayoutInflater mInflater = null;
        public PartListAdapter(Context context, List<PartInfo> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.part_list_item, null);
            }
            PartInfo info = getItem(position);
            ((TextView)convertView.findViewById(R.id.part_desc)).setText(info.subject);
            ((TextView)convertView.findViewById(R.id.recent_ok)).setText(info.recent);
            ((TextView)convertView.findViewById(R.id.ave_count)).setText(info.ave_count);
            return convertView;
        }
    }

    /**
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if( item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}