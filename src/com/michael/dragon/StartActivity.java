package com.michael.dragon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.michael.dragon.db.DgBook;
import com.michael.dragon.db.DgDatabase;
import com.michael.dragon.ui.UiFileSelectDialog;
import com.michael.dragon.ui.UiMessageBox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StartActivity extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DgDatabase.initialize(this);

        setContentView(R.layout.activity_start);

        refreshBooks();

        ((ListView)findViewById(R.id.book_list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookInfo info = (BookInfo)parent.getItemAtPosition(position);
                openBook(info.bid);
            }
        });

    }

    /**
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshBooks();
    }

    private void openBook(long bid) {
        Intent intent = new Intent(this, SelectPartActivity.class);
        intent.putExtra(SelectPartActivity.PARAM_BID, bid);
        startActivity(intent);
    }

    private boolean refreshBooks() {
        DgDatabase db = DgDatabase.getInstance();
        long[] bookIds = db.getBookIdList();
        if( bookIds!=null && bookIds.length>0 ) {
            DgBook dgBook = new DgBook();
            ArrayList<BookInfo> books = new ArrayList<BookInfo>(bookIds.length);
            for( int i=0 ; i<bookIds.length ; i++ ) {
                db.getBook(bookIds[i], dgBook);
                books.add( new BookInfo(dgBook) );
            }
            BookListAdapter adapter = new BookListAdapter(this, books);
            ((ListView)findViewById(R.id.book_list)).setAdapter(adapter);
            return true;
        }
        return false;
    }

    private void importDB() {
        UiFileSelectDialog.selectFile(StartActivity.this,null,null,true,R.string.Title_ImportDB,new UiFileSelectDialog.IFileSelectionResult() {
            @Override
            public void fileSelected(File file, File baseDir) {
                DgDatabase db = DgDatabase.getInstance();
                boolean result = db.importFrom(StartActivity.this, file);
                if( result ) {
                    refreshBooks();
                }
                UiMessageBox.confirm(StartActivity.this, "Import", (result)?"Succeeded":"Failed", null);
            }
        });
    }

    /**
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_actionbar_menu, menu);
        return true;
    }

    /**
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId()==R.id.menu_import) {
            importDB();
        }
        return true;
    }

    /**
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DgDatabase.terminate();
    }

    final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    class BookInfo {
        public long bid;
        public String subject;
        public String qcount;
        public String lastAccessed;

        BookInfo(DgBook src) {
            bid = src.getID();
            subject = src.getSubject();
            qcount = Integer.toString(src.getQuestionCount());
            lastAccessed = sDateFormat.format( new Date( src.getLastAccessed() ) );
        }
    }

    class BookListAdapter extends ArrayAdapter<BookInfo> {
        LayoutInflater mInflater = null;
        public BookListAdapter(Context context, List<BookInfo> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.book_list_item, null);
            }
            BookInfo info = getItem(position);
            ((TextView)convertView.findViewById(R.id.subject)).setText(info.subject);
            ((TextView)convertView.findViewById(R.id.q_count)).setText(info.qcount);
            ((TextView)convertView.findViewById(R.id.last_access)).setText(info.lastAccessed);
            return convertView;
        }
    }
}
