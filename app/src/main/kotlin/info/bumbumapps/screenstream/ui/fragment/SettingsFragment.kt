package info.bumbumapps.screenstream.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.elvishew.xlog.XLog
import com.google.android.material.tabs.TabLayoutMediator
import info.bumbumapps.screenstream.R
import info.bumbumapps.screenstream.data.other.getLog
import info.bumbumapps.screenstream.databinding.FragmentSettingsBinding
import info.bumbumapps.screenstream.ui.viewBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding by viewBinding { fragment -> FragmentSettingsBinding.bind(fragment.requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vpFragmentSettings.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 4

            override fun createFragment(position: Int): Fragment =
                when (position) {
                    0 -> SettingsInterfaceFragment()
                    1 -> SettingsImageFragment()
                    2 -> SettingsSecurityFragment()
                    3 -> SettingsAdvancedFragment()
                    else -> throw IllegalArgumentException("FragmentStateAdapter.getItem: unexpected position: $position")
                }
        }

        TabLayoutMediator(binding.tlFragmentSettings, binding.vpFragmentSettings) { tab, position ->
            tab.text = when (position) {
                0 -> requireContext().getString(R.string.pref_settings_interface)
                1 -> requireContext().getString(R.string.pref_settings_image)
                2 -> requireContext().getString(R.string.pref_settings_security)
                3 -> requireContext().getString(R.string.pref_settings_advanced)
                else -> throw IllegalArgumentException("TabLayoutMediator: unexpected position: $position")
            }
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        XLog.d(getLog("onStart", "Invoked"))
    }

    override fun onStop() {
        XLog.d(getLog("onStop", "Invoked"))
        super.onStop()
    }
}