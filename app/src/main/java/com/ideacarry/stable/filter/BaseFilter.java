package com.ideacarry.stable.filter;

import com.ideacarry.utils.TextureManager;

/**
 * Created by Raining on 2020/9/2.
 */
public abstract class BaseFilter implements IFilter, ITextureManager {
    protected TextureManager mTextureManager;

    @Override
    public void setTextureManager(TextureManager manager) {
        mTextureManager = manager;
    }
}
