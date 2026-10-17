from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase.pdfmetrics import stringWidth
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    KeepTogether,
    NextPageTemplate,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "system-design-concepts-with-examples.md"
OUTPUT = ROOT / "system-design-concepts-with-examples.pdf"

NAVY = colors.HexColor("#16324F")
BLUE = colors.HexColor("#1F5D8F")
PALE_BLUE = colors.HexColor("#EAF2F8")
MID_GRAY = colors.HexColor("#65727E")
LIGHT_GRAY = colors.HexColor("#D9E0E6")
DARK = colors.HexColor("#17212B")


@dataclass(frozen=True)
class ConceptEntry:
    concept_id: str
    category: str
    question: str
    answer: str
    example: str


def field_value(line: str, field: str) -> str:
    prefix = f"**{field}:**"
    if not line.startswith(prefix):
        raise ValueError(f"Expected {prefix}, got: {line}")
    return line[len(prefix) :].strip()


def parse_source() -> list[ConceptEntry]:
    category = ""
    concept_id = ""
    question = ""
    answer = ""
    example = ""
    entries: list[ConceptEntry] = []

    def flush() -> None:
        nonlocal concept_id, question, answer, example
        if not concept_id:
            return
        if not all((category, question, answer, example)):
            raise ValueError(f"Incomplete concept entry: {concept_id}")
        entries.append(ConceptEntry(concept_id, category, question, answer, example))
        concept_id = ""
        question = ""
        answer = ""
        example = ""

    for raw_line in SOURCE.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if raw_line.startswith("## "):
            flush()
            category = raw_line[3:].strip()
        elif raw_line.startswith("### C"):
            flush()
            match = re.match(r"^###\s+(C\d{3})\s+-\s+(.+)$", raw_line)
            if not match:
                raise ValueError(f"Invalid concept heading: {raw_line}")
            concept_id, question = match.groups()
        elif concept_id and line.startswith("**Answer:**"):
            answer = field_value(line, "Answer")
        elif concept_id and line.startswith("**Example:**"):
            example = field_value(line, "Example")
        elif concept_id and line:
            raise ValueError(f"Unexpected content in {concept_id}: {raw_line}")
    flush()

    expected_ids = [f"C{index:03d}" for index in range(1, 121)]
    actual_ids = [entry.concept_id for entry in entries]
    if actual_ids != expected_ids:
        raise ValueError(f"Expected sequential C001-C120, got {actual_ids[:3]}...{actual_ids[-3:]}")
    if len(dict.fromkeys(entry.category for entry in entries)) != 12:
        raise ValueError("Expected exactly 12 concept categories")
    return entries


def styles() -> dict[str, ParagraphStyle]:
    base = getSampleStyleSheet()
    return {
        "title": ParagraphStyle(
            "ConceptGuideTitle",
            parent=base["Title"],
            fontName="Helvetica-Bold",
            fontSize=14.5,
            leading=16.5,
            textColor=NAVY,
            alignment=TA_CENTER,
            spaceAfter=2.2 * mm,
        ),
        "intro": ParagraphStyle(
            "ConceptGuideIntro",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=7.0,
            leading=8.2,
            textColor=MID_GRAY,
            alignment=TA_LEFT,
            spaceAfter=2.2 * mm,
        ),
        "category": ParagraphStyle(
            "ConceptCategory",
            parent=base["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=8.8,
            leading=10.2,
            textColor=colors.white,
            backColor=NAVY,
            borderPadding=(2.1, 3.2, 2.1, 3.2),
            spaceBefore=1.2 * mm,
            spaceAfter=1.2 * mm,
            keepWithNext=True,
        ),
        "question": ParagraphStyle(
            "ConceptQuestion",
            parent=base["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=7.3,
            leading=8.4,
            textColor=BLUE,
            spaceAfter=0.5 * mm,
        ),
        "answer": ParagraphStyle(
            "ConceptAnswer",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=6.65,
            leading=7.75,
            textColor=DARK,
            spaceAfter=0.5 * mm,
        ),
        "example": ParagraphStyle(
            "ConceptExample",
            parent=base["BodyText"],
            fontName="Helvetica-Oblique",
            fontSize=6.55,
            leading=7.65,
            textColor=DARK,
            backColor=PALE_BLUE,
            borderPadding=(1.7, 2.2, 1.7, 2.2),
            spaceAfter=1.3 * mm,
        ),
        "review_note": ParagraphStyle(
            "ConceptReviewNote",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=7.0,
            leading=8.4,
            textColor=DARK,
            spaceAfter=1.5 * mm,
        ),
        "review_cell": ParagraphStyle(
            "ConceptReviewCell",
            parent=base["BodyText"],
            fontName="Helvetica",
            fontSize=6.2,
            leading=7.2,
            textColor=DARK,
        ),
        "review_header": ParagraphStyle(
            "ConceptReviewHeader",
            parent=base["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=6.2,
            leading=7.2,
            textColor=colors.white,
        ),
    }


def draw_footer(canvas, doc, show_divider: bool) -> None:
    canvas.saveState()
    width, height = A4
    if show_divider:
        canvas.setStrokeColor(LIGHT_GRAY)
        canvas.setLineWidth(0.4)
        canvas.line(width / 2, 11 * mm, width / 2, height - 10 * mm)
    canvas.setFont("Helvetica", 6.4)
    canvas.setFillColor(MID_GRAY)
    canvas.drawString(8.5 * mm, 6.5 * mm, "System Design Concepts With Examples - 120 Concepts")
    page_text = f"Page {doc.page}"
    canvas.drawString(width - 8.5 * mm - stringWidth(page_text, "Helvetica", 6.4), 6.5 * mm, page_text)
    canvas.restoreState()


def draw_two_column_page(canvas, doc) -> None:
    draw_footer(canvas, doc, show_divider=True)


def draw_full_width_page(canvas, doc) -> None:
    draw_footer(canvas, doc, show_divider=False)


def build_pdf(entries: list[ConceptEntry]) -> None:
    width, height = A4
    margin_x = 8.5 * mm
    margin_top = 9 * mm
    margin_bottom = 11 * mm
    gap = 6 * mm
    column_width = (width - (2 * margin_x) - gap) / 2
    frame_height = height - margin_top - margin_bottom
    column_frames = [
        Frame(margin_x, margin_bottom, column_width, frame_height, leftPadding=0, rightPadding=0),
        Frame(margin_x + column_width + gap, margin_bottom, column_width, frame_height, leftPadding=0, rightPadding=0),
    ]
    full_width_frame = Frame(
        margin_x,
        margin_bottom,
        width - (2 * margin_x),
        frame_height,
        leftPadding=0,
        rightPadding=0,
    )

    document = BaseDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        leftMargin=margin_x,
        rightMargin=margin_x,
        topMargin=margin_top,
        bottomMargin=margin_bottom,
        title="System Design Concepts With Examples - 120 Concepts",
        author="Instagram Backend Learning Project",
    )
    document.addPageTemplates(
        [
            PageTemplate(id="TwoColumn", frames=column_frames, onPage=draw_two_column_page),
            PageTemplate(id="FullWidth", frames=[full_width_frame], onPage=draw_full_width_page),
        ]
    )
    page_styles = styles()
    story = [
        Paragraph("SYSTEM DESIGN CONCEPTS WITH EXAMPLES", page_styles["title"]),
        Paragraph(
            "Read the question, explain the concept in your own words, and give an example before uncovering the answer. Mark the boxes after a first correct explanation, next-day recall, and seven-day recall. Every entry includes the purpose, trade-off, and a concrete example.",
            page_styles["intro"],
        ),
    ]

    active_category = None
    for entry in entries:
        if entry.category != active_category:
            active_category = entry.category
            story.append(Paragraph(active_category, page_styles["category"]))
        story.append(
            KeepTogether(
                [
                    Paragraph(
                        f"<b>{entry.concept_id}</b>  {entry.question} &nbsp;&nbsp; [ ] [ ] [ ]",
                        page_styles["question"],
                    ),
                    Paragraph(f"<b>Answer:</b> {entry.answer}", page_styles["answer"]),
                    Paragraph(f"<b>Example:</b> {entry.example}", page_styles["example"]),
                ]
            )
        )
        story.append(Spacer(1, 0.1 * mm))

    review_rows = [
        [
            Paragraph("Date", page_styles["review_header"]),
            Paragraph("Concept IDs", page_styles["review_header"]),
            Paragraph("Missing idea / better example", page_styles["review_header"]),
            Paragraph("/5", page_styles["review_header"]),
        ]
    ]
    review_rows.extend([["", "", "", ""] for _ in range(22)])
    review_table = Table(
        review_rows,
        colWidths=[24 * mm, 32 * mm, 123 * mm, 14 * mm],
        rowHeights=[5.5 * mm] + [9.5 * mm] * 22,
        repeatRows=1,
    )
    review_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), NAVY),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("GRID", (0, 0), (-1, -1), 0.35, LIGHT_GRAY),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, PALE_BLUE]),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("LEFTPADDING", (0, 0), (-1, -1), 2),
                ("RIGHTPADDING", (0, 0), (-1, -1), 2),
                ("TOPPADDING", (0, 0), (-1, -1), 1.5),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 1.5),
            ]
        )
    )
    story.extend([NextPageTemplate("FullWidth"), PageBreak()])
    story.append(
        KeepTogether(
            [
                Paragraph("CONCEPT ANSWER SHAPE", page_styles["category"]),
                Paragraph(
                    "<b>1.</b> Define the concept plainly. <b>2.</b> Explain the problem it solves. "
                    "<b>3.</b> State the main trade-off or failure mode. <b>4.</b> Give one concrete system example. "
                    "<b>5.</b> Say when you would not choose it.",
                    page_styles["review_note"],
                ),
                Paragraph("MORNING CONCEPT REVIEW LOG", page_styles["category"]),
                Paragraph(
                    "Record the concepts you explained without notes and the missing idea or weak example to repair before the next recall.",
                    page_styles["review_note"],
                ),
                review_table,
            ]
        )
    )
    document.build(story)


def main() -> None:
    entries = parse_source()
    build_pdf(entries)
    print(f"Generated {OUTPUT.name}: {len(entries)} concepts with examples")


if __name__ == "__main__":
    main()
