package com.edu.bgewallet.bitcoinjtest.base;


public interface BaseView<T extends BasePresenter> {
    void setPresenter(T presenter);
}
