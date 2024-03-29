package com.sessac.travel_agency.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sessac.travel_agency.R
import com.sessac.travel_agency.common.TravelAgencyApplication.Companion.getTravelApplication
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * 모든 프래그먼트에서 사용하는 공통적 스피너, 이미지피커, 바텀시트, 알럿 다이얼로그 팩토리
 */
class CommonHandler {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var onImageSelected: ((Uri) -> Unit)
    private val dialogMap = HashMap<View, BottomSheetDialog>()

    companion object{
        private  var commonHandler: CommonHandler? = null
        fun generateCommonHandler() : CommonHandler{
            if(commonHandler != null){
                return commonHandler as CommonHandler
            }
            return CommonHandler()
        }
    }

    /**
     * [spinner 핸들러]
     * @param items 스피너에서 보여질 item 리스트
     * @param spinner 스피너 레이아웃
     * @param context 프레그먼트 context
     */
    fun spinnerHandler(items: Array<String>, spinner: AutoCompleteTextView, context: Context) {
//        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, items)
        val adapter = ArrayAdapter(getTravelApplication(), android.R.layout.simple_dropdown_item_1line, items)
        spinner.setAdapter(adapter)
    }

    /**
     * [갤러리 선택 이미지 콜백 함수]
     * 1. 갤러리 앱 launch
     * 2. 선택한 image callback
     */
    fun imageSelectAndCallback(activityResultRegistry: ActivityResultRegistry, onImageSelected: (Uri) -> Unit) {
        this.onImageSelected = onImageSelected

        galleryLauncher = activityResultRegistry.register(
            "key",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onImageSelected.invoke(result.data?.data!!)
            }
        }
        //Tutor Pyo Photo Picker
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    /**
     * [날짜 Text 리턴 함수]
     * 시작 날짜 종료 날짜 Text 리턴
     *
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return "startDate ~ endDate"
     *
     */
    fun dateHandler(startDate: Date, endDate: Date): String {
        //tutor pyo
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return dateFormat.format(startDate) + " ~ " + dateFormat.format(endDate)
    }

    /**
     * [day 구하는 함수]
     * 시작 날짜와 종료 날짜를 통해 며칠인지 계산하여 리턴함.
     *
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 일수
     *
     * 시작 날짜와 종료 날짜를 그냥 계산하면 당일은 포함되지 않아 일수를 구할 때 하루가 줄어든 상태가 됨.
     * 따라서 하루를 추가하여 계산
     */
    fun dayCalculator(startDate: Date, endDate: Date): Int {
        return TimeUnit.DAYS.convert(
            abs(endDate.time + TimeUnit.DAYS.toMillis(1) - startDate.time),
            TimeUnit.MILLISECONDS
        ).toInt()
    }

    /**
     * dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme) // 키보드가 레이아웃 가리지 않게 & 모서리 둥글게
     *         dialog.setContentView(view)
     *         dialog.show()*/
    fun showDialog(view: View, context: Context) {
        dialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)  // 키보드가 레이아웃 가리지 않게 & 모서리 둥글게

        // viewGroup 삭제
        if (view.parent != null) (view.parent as ViewGroup).removeView(view)

        dialog.setContentView(view)
        dialog.show()

        dialogMap[view] = dialog
    }

    fun dismissDialog(view: View) {
        dialogMap[view]?.dismiss()
    }

    // 디버깅용 토스트메시지
    fun toastMessage(message: String) {
        Toast.makeText(TravelAgencyApplication.getTravelApplication(), message, Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * 알림 팝업
     * commonHandler.alertDialog(context, 모드) { (yes선택시 처리해야할 함수) }
     *
     * 주의 : 객체 생성 후 꼭 닫아줄 것
     *
     * 모드
     * 0. default - 에러
     * 1. "delete" - 삭제
     * 2. "update" - 수정
     * 3. "create" - 생성
     * 4. "warning" - 주의 - (화면전환시 데이터 날라감 경고)
     *
     * 화면 전환 경고 예시 : 패키지 등록 Toolbar의 Navigation Icon 클릭 시 뒤로 가기 동작 설정
     * binding.toolbar.setNavigationOnClickListener {
     *     // 작성 중인 글이 있을때(기존 데이터에서 변동이 있을때)
     *     val alertDialog = commonHandler.alertDialog(requireContext(), "warning") {
     *         findNavController().popBackStack()  // yes클릭시 해야할 작업(화면전환)
     *     }
     *     alertDialog.dismiss()
     * }
     *
     * 삭제 예시 : 리사이클러뷰 어댑터에게서 넘겨받은 position을 가지고 프래그먼트의 onLongClick에서 deleteTodo함수(db실제 삭제) 실행함
     * overide fun onLongClick(position: Int) {
     *     val alertDialog = commonHandler.alertDialog(requireContext(), "delete") {
     *         delTodo(position) // yes클릭시 해야할 작업(db삭제)
     *     }
     *     alertDialog.dismiss()
     * }
     *
     * 삭제 완료 예시 : db에 삭제 작업 완료 후 삭제되었습니다 alert 띄운다.
     * val alertDialog = commonHandler.alertDialog(requireContext(), "delete") { }
     * alertDialog.dismiss()
     *
     * 수정 완료 예시 : db에 수정 작업 완료 후 수정되었습니다 alert 띄운다.
     * val alertDialog = commonHandler.alertDialog(requireContext(), "update") { }
     * alertDialog.dismiss()
     *
     * 등록 완료 예시 : db에 등록 작업 완료 후 등록되었습니다 alert 띄운다.
     * val alertDialog = commonHandler.alertDialog(requireContext(), "create") { }
     * alertDialog.dismiss()
     *
     * */
    fun alertDialog(context: Context, mode: String, todo: () -> Unit): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)


        // 기본값
        var alertTitleResId = R.string.alert_title_err
        var alertMsgResId = R.string.alert_message_try_again

        when (mode) {
            DELETE -> { //상수값
                alertTitleResId = R.string.alert_title_del
                alertMsgResId = R.string.alert_message_del
                if (todo != null) {
                    builder.setNegativeButton(R.string.alert_no, null)  // 아무 작업 해주지 않으면 null
                    builder.setPositiveButton(R.string.alert_yes) { _, _ ->  // 클릭됐을때 작동할 것을 람다 표현식으로 바로 전달
                        todo.invoke()  // Yes 클릭시 db삭제할 함수
                    }
                } else {
                    // delTodo없는 경우 삭제완료되었습니다 띄움
                    alertMsgResId = R.string.alert_message_del_done
                }
            }

            UPDATE -> {
                alertTitleResId = R.string.alert_title_update
                alertMsgResId = R.string.alert_message_update
            }

            CREATE -> {
                alertTitleResId = R.string.alert_title_create
                alertMsgResId = R.string.alert_message_create
            }

            WARNING -> {
                alertTitleResId = R.string.alert_title_warning
                alertMsgResId = R.string.alert_message_waring
                builder.setNegativeButton(R.string.alert_no, null)
                builder.setPositiveButton(R.string.alert_yes) { _, _ ->  // 클릭됐을때 작동할 것을 람다 표현식으로 바로 전달
                    todo.invoke()  // Yes 클릭시 화면 뒤로가기 함수
                }
            }
        }

        val alertTitle = context.resources.getString(alertTitleResId)
        val alertMsg = context.resources.getString(alertMsgResId)

        builder.setTitle(alertTitle)
        builder.setMessage(alertMsg)

        val alertDialog = builder.create()  // AlertDialog 객체 생성
        alertDialog.show()  // 화면에 표시

        return alertDialog  // 생성된 AlertDialog 객체 반환. 한번 사용 후 dismiss 닫아주기 위함
    }

}