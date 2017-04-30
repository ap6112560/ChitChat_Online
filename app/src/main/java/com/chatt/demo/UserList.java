package com.chatt.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.Contacts;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;

/**
 * The Class UserList is the Activity class. It shows a list of all users of
 * this app. It also shows the Offline/Online status of users.
 */
public class UserList extends CustomActivity
{

	/** The Chat list. */
	private ArrayList<Contacts> uList;

	Contacts copy[];
	//private ArrayList<Contacts> copyList;
	/** The user. */
	public static ParseUser user;

	private int size=0;

	private ArrayList<Bitmap> blist=new ArrayList<Bitmap>();

	private ArrayList<String> checkList=new ArrayList<String>();

	private ArrayList<String> admins=new ArrayList<String>();

	private int menu2status;

	private UserAdapter adp;

	private Date time;

	Thread th;

	public static ArrayList<Date> timeList=new ArrayList<Date>();

	private Menu menu;

	private boolean isRunning;
	/**
	 * The handler.
	 */
	private static Handler handler;

	ListView list;



    /* (non-Javadoc)
         * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
         */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_list);
		menu2status=getIntent().getIntExtra("menu2status",0);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		uList=new ArrayList<Contacts>();
		list = (ListView) findViewById(R.id.list);
		adp=new UserAdapter();
		list.setAdapter(adp);
		handler = new Handler();
		time=user.getUpdatedAt();
		updateUserStatus(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(menu2status==0) {
            updateUserStatus(false);
            timeList.clear();
            th.interrupt();
		}
		//menu2status=0;

	}

	@Override
	protected void onPause() {
		super.onPause();
		isRunning = false;
	}

	/* (non-Javadoc)
         * @see android.support.v4.app.FragmentActivity#onResume()
         */
	@Override
	protected void onResume()
	{
		super.onResume();
		isRunning = true;
		copy=uList.toArray(new Contacts[uList.size()]);
		admins.clear();
		uList.clear();
		blist.clear();
		list.setSelection(0);
		if(menu2status==0){
			findViewById(R.id.btnGrp).setVisibility(View.GONE);

		}
		else
			setTouchNClick(R.id.btnGrp);
		loadUserList();

	}

	/**
	 * Update user status.
	 *
	 * @param online
	 *            true if user is online
	 */
	public static void updateUserStatus(boolean online)
	{
		user.put("online", online);
		try {
			user.save();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load list of counters.
	 */
	private void loadCounterList(){
		th=new Thread(){
			@Override
			public void run() {
				try{
					while (isRunning && !th.isInterrupted()) {
                        int cnt=0;
						handler.post(new Runnable() {
							@Override
							public void run() {
								Parcelable state = list.onSaveInstanceState();
								list.setAdapter(adp);
								list.onRestoreInstanceState(state);
							}
						});
						//adp.notifyDataSetChanged();
						ParseQuery<ParseObject> q2 = ParseQuery.getQuery("Chat");
						for (int i = 0; i < size; i++) {
							q2.whereEqualTo("sender", uList.get(i).getContact());
							q2.whereEqualTo("receiver", user.getUsername());
                            q2.whereGreaterThan("createdAt", timeList.get(i));

							int count = 0;
							try {
								count = q2.count();
                                cnt+=count;
								Log.v("vofni", String.valueOf(count));
								uList.get(i).setCount(count);
								handler.post(new Runnable() {
									@Override
									public void run() {
										adp.notifyDataSetChanged();
									}
								});
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
						}
						for (int i = size; i < uList.size(); i++) {
							q2.whereEqualTo("receiver", uList.get(i).getContact());
							q2.whereNotEqualTo("sender", user.getUsername());
							q2.whereGreaterThan("createdAt", timeList.get(i));
							int count = 0;
							try {
								count = q2.count();
                                cnt+=count;
								uList.get(i).setCount(count);
								handler.post(new Runnable() {
									@Override
									public void run() {
										adp.notifyDataSetChanged();
									}
								});
								//uList.add(copyList.get(i));
								//adp.notifyDataSetChanged();
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
						}
						final int finalCnt = cnt;
						handler.post(new Runnable() {
										 @Override
										 public void run() {
											 menu.findItem(3).setTitle(String.valueOf(finalCnt)+" unread");
										 }
									 });
						sleep(2000);
					}
					}
					catch (InterruptedException e){
						e.printStackTrace();
					}
					catch (RuntimeException e1){
						e1.printStackTrace();
					}
			}
		};
		th.start();
	}
	/**
	 * Load list of users.
	 */
	private void loadUserList()
	{
		final ProgressDialog dia = ProgressDialog.show(this, null,
				getString(R.string.alert_wait));
		ParseUser.getQuery().whereNotEqualTo("username", user.getUsername())
				.findInBackground(new FindCallback<ParseUser>() {

					@Override
					public void done(List<ParseUser> li, ParseException e)
					{
						if (li != null)
						{
							if (li.size() == 0)
								Toast.makeText(UserList.this,
										R.string.msg_no_user_found,
										Toast.LENGTH_SHORT).show();
							else {
								while(menu2status==0 && size<li.size())
								{
									timeList.add(size,time);
									size++;

								}
								//size=li.size();
								for (int i = 0; i <li.size(); i++) {
									ParseUser po=li.get(i);
									ParseFile file=po.getParseFile("image");
									Contacts c=new Contacts(po.getUsername(), file,po.getBoolean("online"));
									uList.add(c);
									admins.add(null);
									adp.notifyDataSetChanged();
								}
							}
						}
						else
						{
							Utils.showDialog(
									UserList.this,
									getString(R.string.err_users) + " "
											+ e.getMessage());
							e.printStackTrace();
						}

						if (menu2status == 0) {
							ParseQuery<ParseObject> q = ParseQuery.getQuery("Group");
							q.whereEqualTo("members", user.getUsername());
                            q.orderByAscending("createdAt");
							q.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> li, ParseException e) {
									if (li != null && li.size() > 0) {
										int k=size;
										for (int i = 0; i < li.size(); i++) {
											ParseObject po = li.get(i);
											Contacts c = new Contacts(po.getString("name"), R.drawable.group);
											uList.add(c);
											admins.add(po.getString("admin"));
											while(k<copy.length && !copy[k].getContact().equals(po.getString("name"))){
												timeList.remove(k);
												k++;
											}
											k++;
											if(timeList.size()==i+size) {
												Log.v("infov", String.valueOf(time));
												timeList.add(time);
											}
											adp.notifyDataSetChanged();
										}
									}
									Log.v("ulist", String.valueOf(uList.size()));
									list.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
									setListener();
									dia.dismiss();
									if(menu2status==0)
										loadCounterList();

								}
							});
						}
						else
						{
							dia.dismiss();
							setListener();
						}
					}
				});
	}

	private void setListener() {
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0,
									View arg1, int pos, long arg3) {
				Bundle b = new Bundle();
				int flag=uList.get(pos).getImgid();
				b.putInt("flag",flag);
				b.putInt("pos",pos);
				if(flag==0)
						b.putParcelable("image", blist.get(pos));
				else {
					b.putInt("image", flag);
					b.putString("admin",admins.get(pos));
				}
				b.putString("username", uList.get(pos).getContact());
				startActivity(new Intent(UserList.this,
						Chat.class).putExtra(
						Const.EXTRA_DATA, b));
			}
		});
	}

	/**
	 * The Class UserAdapter is the adapter class for User ListView. This
	 * adapter shows the user name and it's only online status for each item.
	 */
	private class UserAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return uList.size();
		}

		/* (non-Javadoc)
                 * @see android.widget.Adapter#getItem(int)
                 */
		@Override
		public Contacts getItem(int arg0)
		{
			return uList.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0)
		{
			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(final int pos, View v, ViewGroup arg2)
		{
			if(v==null)
				v = getLayoutInflater().inflate(R.layout.chat_item, null);
			final Contacts c = getItem(pos);
			final CheckBox cb= (CheckBox) v.findViewById(R.id.checkBox);
			TextView count = (TextView) v.findViewById(R.id.counter);
			if(menu2status==0) {
				cb.setVisibility(View.GONE);
				count.setVisibility(View.VISIBLE);
				if(c.getCount()==0)
					count.setBackgroundResource(R.drawable.blank);
				Log.v("cinfo", String.valueOf(c.getCount()));
				count.setText(String.valueOf(c.getCount()));
			}
			else {
				if(checkList.contains(c.getContact()))
					cb.setChecked(true);
				else
					cb.setChecked(false);
				cb.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View view) {

						if(cb.isChecked()) {
							checkList.add(c.getContact());
						}
						else
							checkList.remove(c.getContact());
					}
				});
			}
			TextView lbl = (TextView) v.findViewById(R.id.item);
			ParseFile file = c.getImg();
			BitmapDrawable rightdrawable;
			if(c.getImgid()==0) {
				Bitmap bitmap = null;
				byte[] byteArray = new byte[0];
				try {
					byteArray = file.getData();
					bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
					if(blist.size()==pos)
						blist.add(pos, bitmap);
					//Log.v("blist", String.valueOf(blist.size()-1) + ":" + pos);
				} catch (ParseException e1) {
					Utils.showDialog(
							UserList.this, e1.getMessage());
					e1.printStackTrace();
				}
				rightdrawable = new BitmapDrawable(getResources(), bitmap);
			}
			else
				rightdrawable= (BitmapDrawable) getDrawable(c.getImgid());
			lbl.setText(c.getContact());
			lbl.setCompoundDrawablesWithIntrinsicBounds(
					c.getStatus() ? getResources().getDrawable(R.drawable.ic_online)
							: getResources().getDrawable(R.drawable.ic_offline), null, rightdrawable, null);
			return v;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if(menu2status==0){
			menu.add(0, 1, 0, "Offline Mode");
			menu.add(0, 2, 0, "Create Group");
			menu.add(0,3,0,"").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			this.menu=menu;
		}
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				break;
			case 2:
				//menu2status=1;
				Intent g=getIntent();
				g.putExtra("menu2status",1);
				startActivity(g);
				break;
		}
		return super.onOptionsItemSelected(item);

	}
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.btnGrp) {
			String s="Enter Group Name:";
			checkList.add(user.getUsername());
			//menu2status=0;
			showAlert(s);
		}
	}
	public void showAlert(final String s){
		final AlertDialog.Builder build=new AlertDialog.Builder(UserList.this);
		build.setTitle(s);
		build.setCancelable(false);
		final EditText ed=new EditText(this);
		build.setView(ed);
		build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialogInterface, int i) {
				Log.v("Info",ed.getText().toString());
				final ProgressDialog dia = new ProgressDialog(UserList.this);
				dia.setMessage(getString(R.string.alert_wait));
				dia.show();
				ParseQuery<ParseObject> q = ParseQuery.getQuery("Group");

				q.whereEqualTo("name",ed.getText().toString());

				q.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> li, ParseException e) {
						if (li != null && li.size() > 0) {
							dia.dismiss();
							dialogInterface.cancel();
							showAlert("Group Exists, Enter another name:");
						}
						else
						{
							getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
							ParseObject group = new ParseObject("Group");
							group.put("name",ed.getText().toString());
							group.put("admin",user.getUsername());
							group.addAll("members",checkList);
							group.saveEventually(new SaveCallback() {
								@Override
								public void done(ParseException e) {
									if(e==null) {
										dia.dismiss();
										finish();
									}
									else {
										Utils.showDialog(
												UserList.this, e.getMessage());
										e.printStackTrace();
									}
								}
							});
						}
					}
				});

			}
		});
		build.show();
	}
}
