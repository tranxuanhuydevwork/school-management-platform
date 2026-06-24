import 'package:flutter/material.dart';
import '../../model/RequestForm.dart';

class CreateRequestSheet extends StatefulWidget {
  final Future<bool> Function(RequestType type, String title, String description) onCreate;
  final bool isParent;

  const CreateRequestSheet({
    super.key,
    required this.onCreate,
    required this.isParent,
  });

  @override
  State<CreateRequestSheet> createState() => _CreateRequestSheetState();
}

class _CreateRequestSheetState extends State<CreateRequestSheet> {
  RequestType? _type;
  final _titleCtrl = TextEditingController();
  final _descCtrl  = TextEditingController();
  bool  _loading   = false;

  // LEAVE_REQUEST chỉ hiện với phụ huynh
  List<RequestType> get _availableTypes => RequestType.values
      .where((t) => t != RequestType.leaveRequest || widget.isParent)
      .toList();

  bool get _canSubmit =>
      _type != null &&
          _titleCtrl.text.trim().isNotEmpty &&
          _descCtrl.text.trim().isNotEmpty;

  @override
  void dispose() {
    _titleCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_canSubmit || _loading) return;
    setState(() => _loading = true);

    final ok = await widget.onCreate(
      _type!,
      _titleCtrl.text.trim(),
      _descCtrl.text.trim(),
    );

    if (mounted) {
      setState(() => _loading = false);
      if (ok) Navigator.pop(context, true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.of(context).viewInsets.bottom;

    return Container(
      padding: EdgeInsets.fromLTRB(20, 8, 20, 20 + bottom),
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Handle bar
          Center(
            child: Container(
              width: 40, height: 4,
              margin: const EdgeInsets.only(bottom: 16),
              decoration: BoxDecoration(
                color: Colors.grey.shade300,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
          ),

          const Text('Tạo đơn mới',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),

          // ── Chọn loại đơn ──────────────────────────────
          const Text('Loại đơn *',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8, runSpacing: 8,
            children: _availableTypes.map((t) {
              final sel = _type == t;
              return GestureDetector(
                onTap: () => setState(() => _type = t),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 150),
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(
                    color: sel ? t.color : t.color.withOpacity(0.08),
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(
                        color: sel ? t.color : t.color.withOpacity(0.3)),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(t.icon, size: 14,
                          color: sel ? Colors.white : t.color),
                      const SizedBox(width: 6),
                      Text(t.label,
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                            color: sel ? Colors.white : t.color,
                          )),
                    ],
                  ),
                ),
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // ── Tiêu đề ────────────────────────────────────
          const Text('Tiêu đề *',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          TextField(
            controller: _titleCtrl,
            onChanged: (_) => setState(() {}),
            decoration: _deco('Nhập tiêu đề đơn...'),
          ),
          const SizedBox(height: 14),

          // ── Nội dung ───────────────────────────────────
          const Text('Nội dung *',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          TextField(
            controller: _descCtrl,
            onChanged: (_) => setState(() {}),
            maxLines: 3,
            decoration: _deco('Mô tả chi tiết lý do, mục đích...'),
          ),
          const SizedBox(height: 20),

          // ── Nút gửi ────────────────────────────────────
          SizedBox(
            width: double.infinity,
            height: 50,
            child: ElevatedButton(
              onPressed: _canSubmit ? _submit : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.orange,
                disabledBackgroundColor: Colors.grey.shade200,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14)),
              ),
              child: _loading
                  ? const SizedBox(
                  width: 20, height: 20,
                  child: CircularProgressIndicator(
                      color: Colors.white, strokeWidth: 2))
                  : const Text('Gửi đơn',
                  style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.bold,
                      color: Colors.white)),
            ),
          ),
        ],
      ),
    );
  }

  InputDecoration _deco(String hint) => InputDecoration(
    hintText: hint,
    hintStyle: TextStyle(color: Colors.grey.shade400, fontSize: 13),
    filled: true,
    fillColor: const Color(0xFFF5F7FA),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide.none,
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: const BorderSide(color: Colors.orange, width: 1.5),
    ),
    contentPadding:
    const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
  );
}