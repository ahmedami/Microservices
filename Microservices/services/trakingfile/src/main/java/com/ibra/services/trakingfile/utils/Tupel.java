package com.ibra.services.trakingfile.utils;

public class Tupel<E extends Comparable<E>, T extends Comparable<T>> implements Comparable<Tupel<E, T>>
{
	private E first;
	private T second;

	public Tupel(E first, T second)
	{
		this.setFirst(first);
		this.setSecond(second);
	}

	@Override
	public int hashCode()
	{
		int hashCode = 31 * ((null == first) ? 0 : first.hashCode());
		return hashCode + (null == second ? 0 : second.hashCode());
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other)
	{
		try
		{
			Tupel<E, T> tmp = (Tupel<E, T>) other;
			return tmp.first.equals(this.first) && tmp.second.equals(this.second);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	@Override
	public int compareTo(Tupel<E, T> other)
	{
		if (this.first.compareTo(other.getFirst()) == 0)
		{
			return this.second.compareTo(other.getSecond());
		}
		return other.getFirst().compareTo(this.first);
	}

	public void setFirst(E first)
	{
		this.first = first;
	}

	public E getFirst()
	{
		return first;
	}

	public void setSecond(T second)
	{
		this.second = second;
	}

	public T getSecond()
	{
		return second;
	}
}
