package com.chatt.demo.model;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Date;

import com.chatt.demo.UserList;
import com.parse.ParseFile;

/**
 * The Class Contacts is a Java Bean class that represents a single contact
 */
public class Contacts {

    /**
     * The contact.
     */
    private String contact;
    /**
     * The img.
     */
    private ParseFile img;
    /**
     * The status.
     */
    private boolean status;
    /**
     * The imgid
     **/
    private int imgid = 0;
    /**
     * The count
     **/
    private int count = 0;

    /**
     * Instantiates a new contact.
     *
     * @param contact the contact
     * @param img     the img
     * @param status   the status
     */
    public Contacts(String contact, ParseFile img,boolean status) {
        this.contact = contact;
        this.img = img;
        this.status = status;
    }

    /**
     * Instantiates a new contact.
     *
     * @param contact the contact
     * @param imgid   the imgid
     */
    public Contacts(String contact, int imgid) {
        this.contact = contact;
        this.img =null;
        this.imgid = imgid;
        this.status = false;
    }

    /**
     * Instantiates a new contact.
     */
    public Contacts() {
    }

    /**
     * Gets the contact.
     *
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * Sets the contact.
     *
     * @param contact the new contact
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * Gets the img.
     *
     * @return the img
     */
    public ParseFile getImg() {
        return img;
    }

    /**
     * Sets the img.
     *
     * @param img the new img
     */
    public void setImg(ParseFile img) {
        this.img = img;
    }

    /**
     * Gets the imgid.
     *
     * @return the imgid
     */
    public int getImgid() {
        return imgid;
    }

    /**
     * Sets the imgid.
     *
     * @param imgid the new imgid
     */
    public void setImgid(int imgid) {
        this.imgid = imgid;
    }
    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count.
     *
     * @param count the new count
     */
    public void setCount(int count) {
        this.count = count;
    }
    /**
     * Gets the status.
     *
     * @return the status
     */
    public boolean getStatus()
    {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the new status
     */
    public void setStatus(boolean status)
    {
        this.status = status;
    }
}