package com.chatt.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.StreamHandler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.Contacts;
import com.chatt.demo.model.Conversation;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.Utils;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * The Class Chat is the Activity class that holds main chat screen. It shows
 * all the conversation messages between two users and also allows the user to
 * send and receive messages.
 */
public class Chat extends CustomActivity {

	/**
	 * The Conversation list.
	 */
	private ArrayList<Conversation> convList;

	/**
	 * The chat adapter.
	 */
	private ChatAdapter adp;

	/**
	 * The Editext to compose the message.
	 */
	private EmojiconEditText txt;

	private ImageView btn;

	private String admin=null;

	/**
	 * The user name of buddy.
	 */
	private String buddy;

	/**
	 * The date of last message in conversation.
	 */
	private Date lastMsgDate;

	/**
	 * Flag to hold if the activity is running or not.
	 */
	private boolean isRunning;

	/**
	 * The handler.
	 */
	private static Handler handler;

	Bitmap bitmap = null;

	Uri selectedImage = null;

	EmojIconActions emojIcon;

	byte[] byteImg = new byte[0];

	int f=0;

	int flag;

	int pos;
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);

		convList = new ArrayList<Conversation>();
		ListView list = (ListView) findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setStackFromBottom(true);
		btn = (ImageView) findViewById(R.id.emoji_btn);
		txt = (EmojiconEditText) findViewById(R.id.txt);
		txt.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		View rootView = findViewById(R.id.layout);
		emojIcon = new EmojIconActions(this, rootView, txt, btn);
		emojIcon.ShowEmojIcon();
		emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
			@Override
			public void onKeyboardOpen() {
				Log.e("Keyboard", "open");
			}

			@Override
			public void onKeyboardClose() {
				Log.e("Keyboard", "close");
			}
		});
		setTouchNClick(R.id.btnSend);
		ImageView iv = (ImageView) findViewById(R.id.attach_btn);
		iv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				i.setType("image/* video/*");
				startActivityForResult(i, 1);
			}
		});
		Bundle b = getIntent().getBundleExtra(Const.EXTRA_DATA);
		buddy = b.getString("username");
		pos=b.getInt("pos");
		getActionBar().setTitle(buddy);
		flag=b.getInt("flag");
		if(flag==0)
		{
			bitmap = b.getParcelable("image");
			BitmapDrawable logo = new BitmapDrawable(getResources(), bitmap);
			getActionBar().setLogo(logo);
		}
		else {
			getActionBar().setLogo(flag);
			admin = b.getString("admin");
		}
		handler = new Handler();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		isRunning = true;
		loadConversationList();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		isRunning = false;
	}

	/* (non-Javadoc)
	 * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.btnSend) {
			sendMessage();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(UserList.user.getUsername().equals(admin))
		menu.add(0, 1, 0, "Delete Group");
		return super.onCreateOptionsMenu(menu);
	}
	/**
	 * Call this method to Send message to opponent. It does nothing if the text
	 * is empty otherwise it creates a Parse object for Chat message and send it
	 * to Parse server.
	 */
	private void sendMessage() {
		if (txt.length() == 0 && selectedImage == null)
			return;

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);
		if (selectedImage == null) {
			String s = txt.getText().toString();
			final Conversation c = new Conversation(s, new Date(),
					UserList.user.getUsername());

			c.setStatus(Conversation.STATUS_SENDING);
			convList.add(c);
			adp.notifyDataSetChanged();
			txt.setText(null);
			ParseObject po = new ParseObject("Chat");
			po.put("sender", UserList.user.getUsername());
			po.put("receiver", buddy);
			po.put("message", s);
			po.saveEventually(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null)
						c.setStatus(Conversation.STATUS_SENT);
					else
						c.setStatus(Conversation.STATUS_FAILED);
					adp.notifyDataSetChanged();
				}
			});
		} else {
			String s=null;
			if(f==0)
				s="img.png";
			else if(f==1)
				s="vid.mp4";
			final ParseFile	file = new ParseFile(s, byteImg);
			final ProgressDialog dia = ProgressDialog.show(this, null, getString(R.string.alert_wait));
			file.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException e) {
					if (e == null) {
						final Conversation c = new Conversation(selectedImage, new Date(),
								UserList.user.getUsername(),f);
						c.setStatus(Conversation.STATUS_SENDING);
						convList.add(c);
						adp.notifyDataSetChanged();
						selectedImage = null;
						ParseObject po = new ParseObject("Chat");
						po.put("sender", UserList.user.getUsername());
						po.put("receiver", buddy);
						if(f==0)
							po.put("img", file);
						else if(f==1)
							po.put("vid", file);
						f=0;
						po.saveEventually(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null)
									c.setStatus(Conversation.STATUS_SENT);
								else
									c.setStatus(Conversation.STATUS_FAILED);
								adp.notifyDataSetChanged();
							}
						});
					} else {
						Utils.showDialog(
								Chat.this,
								e.getMessage());
						e.printStackTrace();
					}
					dia.dismiss();
				}
			});
		}
	}

	/**
	 * Load the conversation list from Parse server and save the date of last
	 * message that will be used to load only recent new messages
	 */
	private void loadConversationList() {
		ParseQuery<ParseObject> q = ParseQuery.getQuery("Chat");
		if (convList.size() == 0) {
			// load all messages...
			if(flag==0) {
				ArrayList<String> al = new ArrayList<String>();
				al.add(buddy);
				al.add(UserList.user.getUsername());
				q.whereContainedIn("sender", al);
				q.whereContainedIn("receiver", al);
			}
			else
			{
				q.whereEqualTo("receiver",buddy);
			}
		} else {
			// load only newly received message..
			if (lastMsgDate != null)
				q.whereGreaterThan("createdAt", lastMsgDate);
			if(flag==0) {
				q.whereEqualTo("sender", buddy);
				q.whereEqualTo("receiver", UserList.user.getUsername());
			}
			else{
				q.whereNotEqualTo("sender",UserList.user.getUsername());
				q.whereEqualTo("receiver",buddy);
			}
		}
		q.orderByDescending("createdAt");
		q.setLimit(30);
		q.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> li, ParseException e) {
				if (li != null && li.size() > 0) {
					for (int i = li.size() - 1; i >= 0; i--) {
						Conversation c = null;
						ParseObject po = li.get(i);
						ParseFile file = po.getParseFile("img");
						if (file != null) {
							c = new Conversation(Uri.parse(file.getUrl()), po.getCreatedAt(), po
									.getString("sender"),0);
						}
						else if(null!=(file=po.getParseFile("vid"))){
							c = new Conversation(Uri.parse(file.getUrl()), po.getCreatedAt(), po
									.getString("sender"),1);
						}
						else {
							c = new Conversation(po
									.getString("message"), po.getCreatedAt(), po
									.getString("sender"));
						}
						convList.add(c);
						if (lastMsgDate == null
								|| lastMsgDate.before(c.getDate()))
							lastMsgDate = c.getDate();
						adp.notifyDataSetChanged();
					}
				}
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (isRunning)
							loadConversationList();
					}
				}, 1000);
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UserList.updateUserStatus(true);
		UserList.timeList.set(pos,UserList.user.getUpdatedAt());

	}

	/**
	 * The Class ChatAdapter is the adapter class for Chat ListView. This
	 * adapter shows the Sent or Received Chat message in each list item.
	 */
	private class ChatAdapter extends BaseAdapter {

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return convList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Conversation getItem(int arg0) {
			return convList.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View v, ViewGroup arg2) {
			final Conversation c = getItem(pos);
			if (c.getDiff() == 0) {
				if (c.isSent())
					v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);
				else
					v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);
				TextView lbl = (EmojiconTextView) v.findViewById(R.id.lbl2);
				lbl.setText(c.getMsg());
			} else {
					if (c.isSent())
						v = getLayoutInflater().inflate(R.layout.chat_img_sent, null);
					else
						v = getLayoutInflater().inflate(R.layout.chat_img_rcv, null);
                    ImageView img = (ImageView) v.findViewById(R.id.imgView);
					if (c.getType()==0) {
						Picasso.with(Chat.this)
								.load(c.getImg().toString())
								.noFade()
								.placeholder(R.drawable.icon_placeholder)
								.into(img);
						Log.v("infov", c.getImg().toString());
					}
					else {
                        Bitmap thumb = null;
                        if(c.getVid().toString().startsWith("content://")){
                            BitmapFactory.Options options=new BitmapFactory.Options();
                            options.inSampleSize=1;
                            String s=c.getVid().toString();
                            thumb= MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), Long.parseLong(s.substring(s.lastIndexOf('/') + 1)),MediaStore.Video.Thumbnails.MINI_KIND,options);
                        }
                        else {
                            MediaMetadataRetriever mediaMetadataRetriever = null;
                            try {
                                mediaMetadataRetriever = new MediaMetadataRetriever();
								mediaMetadataRetriever.setDataSource(c.getVid().toString(), new HashMap<String, String>());
                                thumb = mediaMetadataRetriever.getFrameAtTime();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (mediaMetadataRetriever != null) {
                                    mediaMetadataRetriever.release();
                                }
                            }
                        }
                        BitmapDrawable rightdrawable=new BitmapDrawable(getResources(),thumb);
                        img.setBackground(rightdrawable);
                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(Chat.this,
                                        Play.class).putExtra(
                                        Const.EXTRA_DATA,c.getVid().toString()));
                            }
                        });
					}

			}

			TextView lbl = (TextView) v.findViewById(R.id.lbl1);
			lbl.setText(DateUtils.getRelativeDateTimeString(Chat.this, c
							.getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
					DateUtils.DAY_IN_MILLIS, 0));
			lbl.setPaintFlags(lbl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

			lbl = (TextView) v.findViewById(R.id.lbl3);
			if (c.isSent()) {
				if (c.getStatus() == Conversation.STATUS_SENT)
					lbl.setText("Delivered");
				else if (c.getStatus() == Conversation.STATUS_SENDING)
					lbl.setText("Sending...");
				else
					lbl.setText("Failed");
			} else {
				if(flag==0)
					lbl.setText("");
				else
					lbl.setText("sent by "+c.getSender());
			}
			return v;
		}

	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (flag == 0) {
				Uri path = Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, null, null));
				Intent in = new Intent();
				in.setAction(Intent.ACTION_VIEW);
				in.setData(path);
				startActivity(in);
			} else {
				ParseQuery<ParseObject> q = ParseQuery.getQuery("Group");
				q.whereContains("name",buddy);
				final ProgressDialog dia = ProgressDialog.show(this, null,
						getString(R.string.alert_wait));
				q.findInBackground(new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> li, ParseException e) {
						List po = li.get(0).getList("members");
						String [] items= new String[po.size()];
						items= (String[]) po.toArray(items);
                AlertDialog.Builder build=new AlertDialog.Builder(Chat.this);
				build.setTitle("Members");
                build.setCancelable(true);
		        build.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
						dia.dismiss();
					}
				});

			}
		}
		else if (item.getItemId()==1)
		{
			UserList.timeList.remove(pos);
			ParseQuery<ParseObject> q1 = ParseQuery.getQuery("Group");
			q1.whereEqualTo("name",buddy);
			final ProgressDialog dia = ProgressDialog.show(this, null,
					getString(R.string.alert_wait));
			q1.getFirstInBackground(new GetCallback<ParseObject>() {
				@Override
				public void done(ParseObject object, ParseException e) {
					try {
						object.delete();
						ParseQuery<ParseObject> q2 = ParseQuery.getQuery("Chat");
						q2.whereEqualTo("receiver",buddy);
						q2.findInBackground(new FindCallback<ParseObject>() {
							@Override
							public void done(List<ParseObject> objects, ParseException e) {
								ParseObject.deleteAllInBackground(objects, new DeleteCallback() {
									@Override
									public void done(ParseException e) {
										dia.dismiss();
										finish();
									}
								});
							}
						});
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && data != null) {

			selectedImage = data.getData();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				if (selectedImage.toString().contains("images")) {
					Log.v("infov",selectedImage.toString());
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				} else if (selectedImage.toString().contains("video")) {
					Log.v("infov",selectedImage.toString());
					FileInputStream fis = (FileInputStream) getContentResolver().openInputStream(selectedImage);
					int n;
					byte [] buf=new byte[1024];
					while(-1!=(n=fis.read(buf)))
						stream.write(buf,0,n);
					f=1;
				}
				byteImg = stream.toByteArray();
                Log.v("infov", String.valueOf(byteImg.length));
				sendMessage();
			} catch (IOException e) {

				e.printStackTrace();

			}
		}
	}
}