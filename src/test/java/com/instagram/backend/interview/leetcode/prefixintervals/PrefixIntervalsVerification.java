package com.instagram.backend.interview.leetcode.prefixintervals;

import java.lang.reflect.*;
import java.util.*;
public class PrefixIntervalsVerification {
  static final String ROOT="com.instagram.backend.interview.leetcode.prefixintervals.";
  static Method method(String cls,String name,Class<?>... types)throws Exception {
    Method m=Class.forName(ROOT+cls).getDeclaredMethod(name,types);m.setAccessible(true);return m;
  }
  static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
  static int[][] copy(int[][] a){return Arrays.stream(a).map(int[]::clone).toArray(int[][]::new);}
  static boolean[] coverage(int[][] a){boolean[] bits=new boolean[101];for(int[] row:a)for(int x=row[0]*2;x<=row[1]*2;x++)bits[x]=true;return bits;}
  static void checkIntervals(int[][] expectedCoverage,int[][] result){
    for(int i=0;i<result.length;i++){
      require(result[i][0]<=result[i][1],"inverted output");
      if(i>0)require(result[i-1][1]<result[i][0],"overlapping or unordered output");
    }
    require(Arrays.equals(coverage(expectedCoverage),coverage(result)),"coverage differs, including half-points");
  }
  public static void main(String[] args)throws Exception {
    Method sub=method("subarraysum.SubarraySumEqualsKSolution","subarraySum",int[].class,int.class);
    Method product=method("productexceptself.ProductExceptSelfSolution","productExceptSelf",int[].class);
    Method merge=method("mergeintervals.MergeIntervalsSolution","merge",int[][].class);
    Method insert=method("insertinterval.InsertIntervalSolution","insert",int[][].class,int[].class);
    Class<?> num=Class.forName(ROOT+"rangesum.RangeSumQuerySolution$NumArray");
    Constructor<?> constructor=num.getDeclaredConstructor(int[].class);constructor.setAccessible(true);
    Method query=num.getDeclaredMethod("sumRange",int.class,int.class);query.setAccessible(true);
    Random random=new Random(20261017L);int queries=0;
    for(int trial=0;trial<500;trial++) {
      int n=1+random.nextInt(7);int[] a=new int[n];for(int i=0;i<n;i++)a[i]=random.nextInt(5)-2;
      int[] before=a.clone();Object data=constructor.newInstance((Object)a);
      for(int l=0;l<n;l++)for(int r=l;r<n;r++){
        int expected=0;for(int i=l;i<=r;i++)expected+=a[i];
        require((int)query.invoke(data,l,r)==expected,"range sum mismatch");queries++;
      }
      for(int target=-5;target<=5;target++){
        int expected=0;for(int l=0;l<n;l++)for(int r=l;r<n;r++){
          int sum=0;for(int i=l;i<=r;i++)sum+=a[i];if(sum==target)expected++;
        }
        require((int)sub.invoke(null,a,target)==expected,"subarray count mismatch");
      }
      int[] expectedProduct=new int[n];for(int i=0;i<n;i++){
        expectedProduct[i]=1;for(int j=0;j<n;j++)if(j!=i)expectedProduct[i]*=a[j];
      }
      require(Arrays.equals((int[])product.invoke(null,(Object)a),expectedProduct),"product mismatch");
      require(Arrays.equals(a,before),"array input mutated");
      int[][] arbitrary=new int[random.nextInt(7)][2];
      for(int[] row:arbitrary){int x=random.nextInt(16),y=random.nextInt(16);row[0]=Math.min(x,y);row[1]=Math.max(x,y);}
      int[][] original=copy(arbitrary);checkIntervals(arbitrary,(int[][])merge.invoke(null,(Object)arbitrary));
      require(Arrays.deepEquals(arbitrary,original),"merge input mutated");
      int[][] ordered=new int[random.nextInt(7)][2];int position=0;
      for(int[] row:ordered){position+=1+random.nextInt(2);row[0]=position;position+=random.nextInt(3);row[1]=position;}
      int x=random.nextInt(25),y=random.nextInt(25);int[] addition={Math.min(x,y),Math.max(x,y)};
      int[][] all=Arrays.copyOf(ordered,ordered.length+1);all[ordered.length]=addition;
      int[][] old=copy(ordered);int[] oldAddition=addition.clone();
      checkIntervals(all,(int[][])insert.invoke(null,ordered,addition));
      require(Arrays.deepEquals(ordered,old)&&Arrays.equals(addition,oldAddition),"insert input mutated");
    }
    System.out.println("Model checks passed: "+queries+" range queries; 5500 subarray cases; 500 products; 500 merge cases; 500 insert cases. Seed=20261017.");
  }
}
