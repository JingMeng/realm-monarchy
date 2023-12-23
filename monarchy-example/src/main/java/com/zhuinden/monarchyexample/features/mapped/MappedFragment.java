package com.zhuinden.monarchyexample.features.mapped;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhuinden.monarchy.Monarchy;
import com.zhuinden.monarchyexample.Dog;
import com.zhuinden.monarchyexample.R;
import com.zhuinden.monarchyexample.RealmDog;
import com.zhuinden.monarchyexample.application.CustomApplication;
import com.zhuinden.monarchyexample.utils.BaseFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by Zhuinden on 2017.12.21..
 * <p>
 * <p>
 * https://www.mongodb.com/docs/realm/sdk/java/quick-starts/quick-start-local/
 */

public class MappedFragment
        extends BaseFragment {
    MappedDogAdapter mappedDogAdapter;

    LiveData<List<Dog>> dogs;
    Observer<List<Dog>> observer = dogs -> {
        mappedDogAdapter.updateData(dogs);
    };

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Inject
    Monarchy monarchy;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CustomApplication.getInjector(context).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapped, container, false);
    }

    private boolean isDebug = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mappedDogAdapter = new MappedDogAdapter();
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mappedDogAdapter);

        if (isDebug) {
            dogs = monarchy.findAllMappedWithChanges(realm -> realm.where(RealmDog.class),
                    from -> Dog.create(from.getName()));
            dogs.observeForever(observer); // detach != destroy in fragments so this is manual
        }


        String realmName = "My Project";
        RealmConfiguration config = new RealmConfiguration
                .Builder()
//                .name(realmName)

                .build();

        /**
         * 1.  上面不设置名字，会存在默认的名字
         *
         *  追踪一下，就会发现是这个名字
         *     public static final String DEFAULT_REALM_NAME = "default.realm";
         *
         *  赋值给了  fileName  ，在 build 的时候使用
         *
         *
         *  2.  不要使用两个 config  ，不然会报错
         *
         */
//        Realm backgroundThreadRealm = Realm.getInstance(config);
        Realm backgroundThreadRealm = Realm.getInstance(monarchy.getRealmConfiguration());

        Log.i("MappedFragment", "------doWithRealm----111111-----为什么没有查到数据----");
        RealmResults<RealmDog> results = backgroundThreadRealm.where(RealmDog.class).findAllAsync();
        Log.i("MappedFragment", "------doWithRealm----这个地方执行了吗？---------" + results.size());
        results.addChangeListener(dogs -> {
            // React to change
            Log.i("MappedFragment", "------doWithRealm-------------");
            Log.i("MappedFragment", "------doWithRealm-------------" + dogs.size());
        });


        HandlerThread handlerThread = new HandlerThread("MONARCHY_REALM-#" + hashCode());
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {


//            monarchy.doWithRealm(realm -> {
//                Log.i("MappedFragment", "------doWithRealm----111111-----为什么没有查到数据----");
//                RealmResults<RealmDog> results = realm.where(RealmDog.class).findAllAsync();
//                Log.i("MappedFragment", "------doWithRealm----这个地方执行了吗？---------" + results.size());
//                results.addChangeListener(dogs -> {
//                    // React to change
//                    Log.i("MappedFragment", "------doWithRealm-------------");
//                    Log.i("MappedFragment", "------doWithRealm-------------" + dogs.size());
//                });
//            });
        });
    }

    @Override
    public void onDestroyView() {

        if (isDebug) {
            dogs.removeObserver(observer);
        }
        super.onDestroyView();
    }
}
