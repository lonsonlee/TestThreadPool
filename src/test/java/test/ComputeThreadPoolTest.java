package test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//计算密集型任务 lonson
//原文见 https://blog.csdn.net/holmofy/article/details/81271839
public class ComputeThreadPoolTest {

	final static ThreadPoolExecutor computeExecutor;

    final static List<Callable<Long>> computeTasks;

    final static int task_count = 5000;

    static {
        computeExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        // 创建5000个计算任务
        computeTasks = new ArrayList<>(task_count);
        for (int i = 0; i < task_count; i++) {
            computeTasks.add(new ComputeTask());
        }
    }

    static class ComputeTask implements Callable<Long> {
        // 计算一至五十万数的总和(纯计算任务)
        @Override
        public Long call() {
            long sum = 0;
            for (long i = 0; i < 50_0000; i++) {
                sum += i;
            }
            return sum;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 我电脑是四核处理器
        int processorsCount = Runtime.getRuntime().availableProcessors();
        // 逐一增加线程池的线程数
        for (int i = 1; i <=  processorsCount * 5; i++) {
            computeExecutor.setCorePoolSize(i);
            computeExecutor.setMaximumPoolSize(i);
            computeExecutor.prestartAllCoreThreads();
            System.out.print(i);
            computeExecutor.invokeAll(computeTasks); // warm up all thread
            System.out.print("\t");
            testExecutor(computeExecutor, computeTasks);
            System.out.println();
            // 一定要让cpu休息会儿，Windows桌面操作系统不会让应用长时间霸占CPU
            // 否则Windows回收应用程序的CPU核心数将会导致测试结果不准确
            TimeUnit.SECONDS.sleep(5);// cpu rest
        }
        computeExecutor.shutdown();
    }

    private static <T> void testExecutor(ExecutorService executor, List<Callable<T>> tasks)
        throws InterruptedException {
        for (int i = 0; i < 8; i++) {
            long start = System.currentTimeMillis();
            executor.invokeAll(tasks); // ignore result
            long end = System.currentTimeMillis();
            System.out.print(end - start); // 记录时间间隔
            System.out.print("\t");
            TimeUnit.SECONDS.sleep(1); // cpu rest
        }
    }
}
/*
 * 1	1964	1959	1954	1943	1953	1950	1968	1972	
2	1084	1103	1099	1142	1072	1123	1112	1088	
3	856	862	871	860	866	864	885	877	
4	747	783	813	764	812	773	824	790	
5	762	779	764	789	758	804	775	815	
6	773	807	783	767	793	800	782	785	
7	743	768	809	767	776	831	836	840	
8	777	827	858	831	773	787	910	842	
9	771	810	813	780	811	806	781	859	
10	760	788	839	772	894	852	811	822	
11	834	871	923	870	901	937	970	758	
12	846	836	865	1085	876	812	780	812	
13	770	826	837	783	786	816	812	785	
14	755	829	789	826	849	773	814	742	
15	836	790	821	778	918	789	818	806	
16	788	803	830	794	847	852	802	779	
17	724	818	792	825	814	861	811	769	
18	785	789	917	807	797	825	841	831	
19	828	883	885	829	827	788	788	906	
20	825	827	918	803	862	842	810	813	
 */
