package tv.ismar.searchpage.core.filter;

public class WordFilterResult
{
	public WordFilterResult(int s, int e, int t)
	{
		start = s;
		end   = e;
		tag   = t;
	}
	
	public int start;
	public int end;
	public int tag;
}