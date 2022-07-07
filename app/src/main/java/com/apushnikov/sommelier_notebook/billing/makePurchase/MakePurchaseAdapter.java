/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apushnikov.sommelier_notebook.billing.makePurchase;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

//import com.apushnikov.other301_mypurchases.R;
//import com.apushnikov.other301_mypurchases.databinding.InventoryHeaderBinding;
//import com.apushnikov.other301_mypurchases.databinding.InventoryItemBinding;
//import com.apushnikov.other301_mypurchases.log_file.LogFile;

import com.apushnikov.sommelier_notebook.R;
import com.apushnikov.sommelier_notebook.databinding.InventoryHeaderBinding;
import com.apushnikov.sommelier_notebook.databinding.InventoryItemBinding;
import com.apushnikov.sommelier_notebook.log_file.LogFile;

import java.util.List;

/**===========================================================================================
 * Базовая реализация адаптера RecyclerView с представлениями заголовка и содержимого.
 * <p>
 * Basic implementation of RecyclerView adapter with header and content views.
 * ===========================================================================================
 */
public class MakePurchaseAdapter extends RecyclerView.Adapter<MakePurchaseAdapter.ViewHolder> {


    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    private LogFile logFile;

    /**VIEW_TYPE_HEADER - если View является заголовком*/
    static public final int VIEW_TYPE_HEADER = 0;
    /**VIEW_TYPE_ITEM - если View является просто пунктом*/
    static public final int VIEW_TYPE_ITEM = 1;

    /**inventoryList - список инвентаря*/
    private final List<Item> inventoryList;

    /**makePurchaseViewModel - Это используется для любой бизнес-логики, а также для вывода LiveData
     * из BillingRepository*/
    private final MakePurchaseViewModel makePurchaseViewModel;

    /**makePurchaseFragment - Этот фрагмент является просто оберткой для инвентаря
     * (т.е. предметов для продажи). Здесь опять же нет сложной биллинговой логики.
     * Вся логика выставления счетов находится внутри [BillingRepository].
     * <p>
     * [BillingRepository] предоставляет так называемый объект [AugmentedSkuDetails],
     * который показывает, что продается, и разрешено ли пользователю покупать товар
     * в данный момент. Например. если у пользователя уже есть полный бак бензина, он не может
     * покупать газ в данный момент.
     */
    private final MakePurchase makePurchase;

    /**=======================================================================================
     * Конструктор MakePurchaseAdapter
     *
     * @param inventoryList             список инвентаря
     * @param makePurchaseViewModel     Это используется для любой бизнес-логики, а также для вывода LiveData
     *                                  из BillingRepository
     * @param makePurchase      Этот фрагмент является просто оберткой для инвентаря
     *                                  (т.е. предметов для продажи)
     * =======================================================================================
     */
    public MakePurchaseAdapter(@NonNull List<Item> inventoryList,
                               @NonNull MakePurchaseViewModel makePurchaseViewModel,
                               @NonNull MakePurchase makePurchase,
                               LogFile logFile) {


        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
//        logFile.writeLogFile("MakePurchaseAdapter: Начало public MakePurchaseAdapter");
        this.logFile = logFile;


        this.inventoryList = inventoryList;
        this.makePurchaseViewModel = makePurchaseViewModel;
        this.makePurchase = makePurchase;

    }

    /**=====================================================================================
     * onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
     *
     * @param parent
     * @param viewType
     * @return
     * =====================================================================================
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        InventoryItemBinding inventoryItemBinding = null;
        InventoryHeaderBinding inventoryHeaderBinding = null;

        switch (viewType) {
            case VIEW_TYPE_HEADER:  // VIEW_TYPE_HEADER - если View является заголовком
                // Раздуваем заголовок inventory_header.xml
                inventoryHeaderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.inventory_header, parent,
                        false);
                // getRoot() - Возвращает самый внешний вид в файле макета, связанном с привязкой.
                // Если эта привязка предназначена для файла макета слияния, это вернет первый
                // корень в теге слияния.
                view = inventoryHeaderBinding.getRoot();
                break;
            default:                // VIEW_TYPE_ITEM - если View является просто пунктом
                // Раздуваем пункт inventory_item.xml
                inventoryItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.inventory_item, parent,
                        false);
                // getRoot() - Возвращает самый внешний вид в файле макета, связанном с привязкой.
                // Если эта привязка предназначена для файла макета слияния, это вернет первый
                // корень в теге слияния.
                view = inventoryItemBinding.getRoot();
                break;
        }

        return new ViewHolder(view, viewType, inventoryHeaderBinding, inventoryItemBinding,
                logFile);
    }

    /**=====================================================================================
     * onBindViewHolder(@NonNull ViewHolder holder, int position)
     *
     * @param holder
     * @param position
     * =====================================================================================
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Item item = inventoryList.get(position);
        holder.bind(item, makePurchaseViewModel, makePurchase);

    }

    /**=====================================================================================
     * getItemCount()
     *
     * @return
     * =====================================================================================
     */
    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    /**=====================================================================================
     * getItemViewType(int position) - возвращает тип представления элемента
     *
     * @param position
     * @return
     * =====================================================================================
     */
    @Override
    public int getItemViewType(int position) {
        return inventoryList.get(position).viewType;
    }

    /**=====================================================================================
     * static class ViewHolder extends RecyclerView.ViewHolder
     *
     * =====================================================================================
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
        private LogFile logFile;

        final InventoryHeaderBinding inventoryHeaderBinding;
        final InventoryItemBinding inventoryItemBinding;

        /**================================================================================
         * ViewHolder - конструктор класа
         *
         * @param v
         * @param viewType
         * @param inventoryHeaderBinding
         * @param inventoryItemBinding
         * ================================================================================
         */
        public ViewHolder(View v, int viewType, InventoryHeaderBinding inventoryHeaderBinding,
                          InventoryItemBinding inventoryItemBinding,
                          LogFile logFile) {
            super(v);


            /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
             * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
             */
//            logFile.writeLogFile("MakePurchaseAdapter: ViewHolder: Начало public ViewHolder");
            this.logFile = logFile;

            this.inventoryHeaderBinding = inventoryHeaderBinding;
            this.inventoryItemBinding = inventoryItemBinding;

        }

        /**================================================================================
         * bind - привязка пунктов
         *
         * @param item
         * @param makePurchaseViewModel
         * @param makePurchase
         * ================================================================================
         */
        void bind(Item item,
                  MakePurchaseViewModel makePurchaseViewModel,
                  MakePurchase makePurchase) {

            switch (item.viewType) {
                case VIEW_TYPE_HEADER:  // VIEW_TYPE_HEADER - если View является заголовком
                    inventoryHeaderBinding.headerTitle.setText(item.getTitleOrSku());
                    inventoryHeaderBinding.headerTitle.setMovementMethod(LinkMovementMethod.getInstance());
                    inventoryHeaderBinding.setLifecycleOwner(makePurchase);
                    inventoryHeaderBinding.executePendingBindings();
                    break;
                default:                // VIEW_TYPE_ITEM - если View является просто пунктом
                    inventoryItemBinding.setSku(item.getTitleOrSku().toString());
                    inventoryItemBinding.setSkuDetails(
                            makePurchaseViewModel.getSkuDetails(item.getTitleOrSku().toString()));
                    inventoryItemBinding.skuTitle.setMovementMethod(LinkMovementMethod.getInstance());
                    inventoryItemBinding.setMakePurchaseFragment(makePurchase);
                    inventoryItemBinding.setLifecycleOwner(makePurchase);
                    inventoryItemBinding.executePendingBindings();
                    break;
            }
        }
    }

    /**================================================================================
     * static class Item
     * <p>
     * Элемент, который будет отображаться в нашем RecyclerView.
     * <p>
     * Каждый элемент содержит одну строку: либо заголовок заголовка, либо ссылку на артикул,
     * в зависимости от типа представления.
     * <p>
     * An item to be displayed in our RecyclerView. Each item contains a single string: either
     * the title of a header or a reference to a SKU, depending on what the type of the view is.
     * ================================================================================
     */
    static class Item {

        private final @NonNull
        CharSequence titleOrSku;
        private final int viewType;

        /**===========================================================================
         * Конструктор Item
         *
         * @param titleOrSku    Заголовок или Sku
         * @param viewType      Тип
         * ===========================================================================
         */
        public Item(@NonNull CharSequence titleOrSku, int viewType) {
            this.titleOrSku = titleOrSku;
            this.viewType = viewType;
        }

        /**===========================================================================
         * Геттер getTitleOrSku() - возвращает Заголовок или Sku
         *
         * @return
         * ===========================================================================
         */
        public @NonNull
        CharSequence getTitleOrSku() {
            return titleOrSku;
        }

        /**===========================================================================
         * Геттер getViewType() - возвращает тип
         *
         * @return
         * ===========================================================================
         */
        public int getViewType() {
            return viewType;
        }


    }
}