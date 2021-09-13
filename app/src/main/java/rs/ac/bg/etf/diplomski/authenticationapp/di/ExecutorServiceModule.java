package rs.ac.bg.etf.diplomski.authenticationapp.di;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public interface ExecutorServiceModule {

    @Provides
    static ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(4);
    }
}
