package com.itsdf07.bluetoothchat.common;

import android.util.SparseArray;
import android.view.View;

/**
 * 关键点：
 * SparseArray(稀疏数组).Android内部特有的api,标准的jdk是没有这个类的.
 * 在Android内部用来替代HashMap<Integer,E>这种形式,使用SparseArray更加节省内存空间的使用,
 * SparseArray也是以key和value对数据进行保存的.使用的时候只需要指定value的类型即可.并且key不需要封装成对象类型.
 * <p>
 * Created by itsdf07 on 2017/5/4 11:06.
 * E-Mail: 923255742@qq.com
 * GitHub: https://github.com/itsdf07
 */

public class CommonViewHolder {
    /**
     * @param view 所有缓存View的根View
     * @param id   缓存View的唯一标识
     * @return
     */
    public static <T extends View> T get(View view, int id) {

        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        //如果根view没有用来缓存View的集合
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);//创建集合和根View关联
        }
        View chidlView = viewHolder.get(id);//获取根View储存在集合中的子View
        if (chidlView == null) {//如果没有该子View
            //找到该子View
            chidlView = view.findViewById(id);
            viewHolder.put(id, chidlView);//保存到集合
        }
        return (T) chidlView;
    }
//原ViewHolder使用方式
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder viewHolder = null;
//        if (convertView == null) {
//            convertView = LayoutInflater.from(context)
//                    .inflate(R.layout.item, parent, false);
//            viewHolder = new ViewHolder();
//            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//
//        viewHolder.name.setText("sss");
//
//        return convertView;
//    }
//
//    private static class ViewHolder {
//        TextView name;
//    }

//    最后经过优化和简化后的使用方式
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        if (convertView == null) {
//            convertView = LayoutInflater.from(context)
//                    .inflate(R.layout.item, parent, false);
//        }
//
//        TextView name = CommonViewHolder.get(convertView, R.id.name);
//
//        name.setText("aso");
//
//        return convertView;
//    }


}
