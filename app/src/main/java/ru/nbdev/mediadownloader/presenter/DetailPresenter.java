package ru.nbdev.mediadownloader.presenter;

import android.graphics.drawable.Drawable;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import moxy.InjectViewState;
import moxy.MvpPresenter;
import ru.nbdev.mediadownloader.R;
import ru.nbdev.mediadownloader.common.image_loader.ImageLoader;
import ru.nbdev.mediadownloader.common.media_manager.MediaManager;
import ru.nbdev.mediadownloader.model.entity.Photo;
import ru.nbdev.mediadownloader.model.repository.PhotoRepository;
import ru.nbdev.mediadownloader.view.detail.DetailView;
import timber.log.Timber;

@InjectViewState
public class DetailPresenter extends MvpPresenter<DetailView> {

    private final String IMAGE_LOADER_TAG = "DetailPresenter";
    private final long photoId;
    private final CompositeDisposable compositeDisposable;
    private Photo photo;

    @Inject
    MediaManager mediaManager;

    @Inject
    ImageLoader imageLoader;

    @Inject
    PhotoRepository photoRepository;


    public DetailPresenter(long photoId) {
        this.photoId = photoId;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onFirstViewAttach() {
        if (photo == null) {
            loadPhotoById(photoId);
        } else {
            getViewState().showPhoto(photo);
        }
        Timber.d("Photo ID = %d", photoId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageLoader.cancelLoading(IMAGE_LOADER_TAG);
        compositeDisposable.clear();
    }

    public void onSaveClick() {
        if (photo == null) {
            getViewState().showMessage(R.string.load_error);
            return;
        }
        getViewState().checkWriteStoragePermissions();
    }

    public void onShareClick() {
        if (photo == null) {
            getViewState().showMessage(R.string.load_error);
            return;
        }
        mediaManager.sharePhoto(photo);
    }

    public void onWriteStoragePermissionReady(boolean isGranted) {
        if (isGranted) {
            Disposable disposable = mediaManager.downloadPhoto(photo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        mediaManager.updateAndroidGallery(file);
                        getViewState().showMessage(R.string.saved_to, " " + file.toString());
                    }, throwable -> getViewState().showMessage(R.string.save_error));
            compositeDisposable.add(disposable);
        } else {
            getViewState().showMessage(R.string.storage_permission_needed);
        }
    }

    private void loadPhotoById(long id) {
        getViewState().showProgress();
        Disposable disposable = photoRepository.getPhotoById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        photo -> {
                            this.photo = photo;
                            showPhoto(photo);
                        },
                        throwable -> {
                            Timber.e(throwable, "photoRepository.loadPhotoById() error.");
                            getViewState().showError();
                            getViewState().showMessage(R.string.load_error);
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void showPhoto(Photo photo) {
        imageLoader.loadImageFromUrl(photo.fullSizeURL, IMAGE_LOADER_TAG, new ImageLoader.OnReadyListener() {

            @Override
            public void onSuccess(Drawable image) {
                photo.setDrawable(image);
                getViewState().showPhoto(photo);
            }

            @Override
            public void onError() {
                Timber.e("imageLoader.loadImage() error.");
                getViewState().showError();
                getViewState().showMessage(R.string.load_error);
            }
        });
    }
}
