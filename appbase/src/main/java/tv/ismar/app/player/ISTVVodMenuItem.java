package tv.ismar.app.player;

import java.util.ArrayList;

public class ISTVVodMenuItem{
	protected String  title;
	protected int     id=-1;
	protected boolean enabled;
	protected boolean selected;
	public boolean isSub=false;
	protected ArrayList<ISTVVodMenuItem> subItems;

	ISTVVodMenuItem(int id, String title, boolean en, boolean sel){
		this.id    = id;
		this.title = title;
		this.enabled  = en;
		this.selected = sel;
	}
	
	ISTVVodMenuItem(int id, String title){
		this.id    = id;
		this.title = title;
		this.enabled  = true;
		this.selected = false;
	}

	public ISTVVodMenuItem addItem(int id, String title, boolean en, boolean sel){
		if(subItems==null){
			subItems = new ArrayList<ISTVVodMenuItem>();
		}

		ISTVVodMenuItem item = new ISTVVodMenuItem(id, title, en, sel);

		subItems.add(item);

		return item;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public void setTitle(int id, String title){
		ISTVVodMenuItem item = findItem(id);

		if(item!=null){
			item.setTitle(title);
		}
	}

	public void clear(){
		subItems = null;
	}


	public ISTVVodMenuItem addItem(int id, String title){
		return addItem(id, title, true, false);
	}

	public ISTVVodMenuItem addSubMenu(int id, String title, boolean en, boolean sel){
		ISTVVodMenuItem item;

		item = addItem(id, title, en, sel);

		item.isSub=true;

		return item;
	}

	public ISTVVodMenuItem addSubMenu(int id, String title){
		return addSubMenu(id, title, true, false);
	}

	public ISTVVodMenuItem findItem(int id){
		if(this.id==id)
			return this;

		if(subItems!=null){
			for(ISTVVodMenuItem item : subItems){
				ISTVVodMenuItem find = item.findItem(id);
				if(find!=null)
					return find;
			}
		}

		return null;
	}

	public void enable(){
		enabled = true;
	}

	public void disable(){
		enabled = false;
	}

	public void select(){
		selected = true;
	}

	public void unselect(){
		selected = false;
	}

	public boolean isEnabled(){
		return enabled;
	}

	public boolean isSelected(){
		return selected;
	}
}

