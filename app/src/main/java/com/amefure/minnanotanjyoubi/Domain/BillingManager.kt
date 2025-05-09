import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.QueryProductDetailsParams.Product
import kotlinx.coroutines.CompletableDeferred

/**
 * エミュレーターでは商品情報を取得できない
 */
class BillingManager(context: Context) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    /** 課金対象の商品情報 */
    public var productDetailsList: List<ProductDetails> = emptyList()
    /** 購入処理の結果 */
    private var purchaseResult: CompletableDeferred<Result<Purchase>>? = null

    /** 初期化処理 */
    suspend fun initialize() {
        // 接続処理が完了するまで待機させるためのCompletableDeferred
        val connectionDeferred = CompletableDeferred<Boolean>()
        // Google Playの課金サービスへ接続
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("InAPP" , "課金サービスへ接続成功")
                    // 成功を通知
                    connectionDeferred.complete(true)
                } else {
                    // 接続失敗
                    connectionDeferred.completeExceptionally(
                        IllegalStateException("Billing Setup failed: ${result.debugMessage}")
                    )
                }
            }

            /** BillingServiceから切断された */
            override fun onBillingServiceDisconnected() {
                // 再接続の考慮が必要な場合はここに処理を書く
            }
        })
        // 接続処理の結果を待機
        connectionDeferred.await()
        productDetailsList = queryProductDetailsList()
    }

    /** 商品情報リスト取得処理 */
    private suspend fun queryProductDetailsList(): List<ProductDetails> {
        // 商品取得処理が完了するまで待機させるためのCompletableDeferred
        val queryDeferred = CompletableDeferred<List<ProductDetails>>()
        // 取得対象の商品情報を構築
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    createProduct(PRODUCT_ITEM_ID, ProductType.INAPP)
                )
            ).build()

        // 商品情報問い合わせ
        billingClient.queryProductDetailsAsync(params) { billingResult, productList ->
            // billingResult => 問い合わせ結果のステータス
            // productList => 成功すれば商品情報リスト
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productList.isNotEmpty()) {
                Log.d("InAPP" , "商品情報取得成功$productList")
                // 取得できた商品情報(ProductDetails)リストを通知
                queryDeferred.complete(productList)
            } else {
                // 取得失敗
                queryDeferred.completeExceptionally(
                    IllegalStateException("ProductDetails fetch failed: ${billingResult.debugMessage}")
                )
            }
        }
        return queryDeferred.await()
    }

    /** 取得用プロダクト情報生成 */
    private fun createProduct(productId: String, productType: String): Product {
        return Product.newBuilder()
            .setProductId(productId)
            .setProductType(productType)
            .build()
    }

    /** 購入フロー */
    suspend fun launchPurchaseFlow(
        activity: Activity,
        details: ProductDetails
    ): Result<Purchase> {
        val purchaseDeferred = CompletableDeferred<Result<Purchase>>()
        // 購入結果用Deferred
        purchaseResult = purchaseDeferred

        // 購入対象の商品を構築
        val billingParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            ).build()

        val result = billingClient.launchBillingFlow(activity, billingParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            purchaseDeferred.complete(Result.failure(Exception(result.debugMessage)))
        }

        return purchaseDeferred.await()
    }

    /** 購入の結果 */
    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val deferred = purchaseResult ?: return

        if (result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
            deferred.complete(Result.success(purchases[0]))
        } else {
            deferred.complete(Result.failure(Exception("Purchase failed: ${result.debugMessage}")))
        }
    }

    fun destroy() {
        billingClient.endConnection()
    }

    companion object {
        private const val PRODUCT_ITEM_ID = "PRODUCT_ITEM_ID"
    }
}