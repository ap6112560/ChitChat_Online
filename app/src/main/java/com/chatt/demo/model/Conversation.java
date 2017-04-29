package com.chatt.demo.model;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Date;

import com.chatt.demo.UserList;

/**
 * The Class Conversation is a Java Bean class that represents a single chat
 * conversation message.
 */
public class Conversation
{

	/** The Constant STATUS_SENDING. */
	public static final int STATUS_SENDING = 0;

	/** The Constant STATUS_SENT. */
	public static final int STATUS_SENT = 1;

	/** The Constant STATUS_FAILED. */
	public static final int STATUS_FAILED = 2;
	/** The msg. */
	private String msg;
	/** The img. */
	private Uri img;
	/** The vid. */
	private Uri vid;
	/** The status. */
	private int status = STATUS_SENT;
	/** The date. */
	private Date date;

	/** The sender. */
	private String sender;
	/** The differentiator**/
	private int diff=0;
	/** The type**/
	private int type=0;
	/**
	 * Instantiates a new conversation.
	 * 
	 * @param msg
	 *            the msg
	 * @param date
	 *            the date
	 * @param sender
	 *            the sender
	 */
	public Conversation(String msg, Date date, String sender)
	{
		this.msg = msg;
		this.img=null;
		this.date = date;
		this.sender = sender;
		this.diff=0;
	}
	/**
	 * Instantiates a new conversation.
	 *
	 * @param file
	 *            the file
	 * @param date
	 *            the date
	 * @param sender
	 *            the sender
	 */
	public Conversation(Uri file, Date date, String sender,int type)
	{
		this.msg =null;
		if(type==0)
			this.img = file;
		else if(type==1)
			this.vid=file;
		this.date = date;
		this.sender = sender;
		this.diff=1;
		this.type=type;
	}

	/**
	 * Instantiates a new conversation.
	 */
	public Conversation()
	{
	}

	/**
	 * Gets the msg.
	 * 
	 * @return the msg
	 */
	public String getMsg()
	{
		return msg;
	}

	/**
	 * Sets the msg.
	 * 
	 * @param msg
	 *            the new msg
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	/**
	 * Gets the img.
	 *
	 * @return the img
	 */
	public Uri getImg()
	{
		return img;
	}

	/**
	 * Sets the img.
	 *
	 * @param img
	 *            the new img
	 */
	public void setImg(Uri img)
	{
		this.img = img;
	}
	/**
	 * Gets the vid.
	 *
	 * @return the vid
	 */
	public Uri getVid()
	{
		return vid;
	}

	/**
	 * Sets the vid.
	 *
	 * @param vid
	 *            the new vid
	 */
	public void setVid(Uri vid)
	{
		this.vid = vid;
	}

	/**
	 * Checks if is sent.
	 * 
	 * @return true, if is sent
	 */
	public boolean isSent()
	{
		return UserList.user.getUsername().equals(sender);
	}

	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date
	 *            the new date
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}

	/**
	 * Gets the sender.
	 * 
	 * @return the sender
	 */
	public String getSender()
	{
		return sender;
	}

	/**
	 * Sets the sender.
	 * 
	 * @param sender
	 *            the new sender
	 */
	public void setSender(String sender)
	{
		this.sender = sender;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}
	/**
	 * Gets the differentiator.
	 *
	 * @return the differentiator
	 */
	public int getDiff()
	{
		return diff;
	}

	/**
	 * Sets the differentiator.
	 *
	 * @param diff
	 *            the new differentiator
	 */
	public void setDiff(int diff)
	{
		this.diff = diff;
	}
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(int type)
	{
		this.type = type;
	}
}
