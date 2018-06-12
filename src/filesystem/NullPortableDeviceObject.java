package filesystem;

import java.math.BigInteger;
import java.util.Date;

import be.derycke.pieter.com.Guid;
import jmtp.PortableDeviceObject;

public class NullPortableDeviceObject implements PortableDeviceObject {
	private static final Date NULL_DATE = new Date();
	private static final NullPortableDeviceObject NULL_PORT_DEV_OBJ = new NullPortableDeviceObject();
	private static final short[] NULL_SHORT_ARR = {0};

	@Override
	public String getID() {
		return "";
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getOriginalFileName() {
		return "";
	}

	@Override
	public boolean canDelete() {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isSystemObject() {
		return false;
	}

	@Override
	public boolean isDrmProtected() {
		return false;
	}

	@Override
	public Date getDateModified() {
		return null;
	}

	@Override
	public Date getDateCreated() {
		return NULL_DATE;
	}

	@Override
	public Date getDateAuthored() {
		return NULL_DATE;
	}

	@Override
	public PortableDeviceObject getParent() {
		return NULL_PORT_DEV_OBJ;
	}

	@Override
	public BigInteger getSize() {
		return BigInteger.ZERO;
	}

	@Override
	public String getPersistentUniqueIdentifier() {
		return "";
	}

	@Override
	public String getSyncID() {
		return "";
	}

	@Override
	public Guid getFormat() {
		return new Guid(0, 0, 0, NULL_SHORT_ARR);
	}

	@Override
	public void setSyncID(String value) { // Do nothing
	}

	@Override
	public void delete() { // Do nothing
	}

}
