import { useState } from 'react';

interface Props {
  text: string;
}

export default function HelpTooltip({ text }: Props) {
  const [visible, setVisible] = useState(false);

  return (
    <span className="help-tooltip-wrapper">
      <span
        className="help-tooltip-icon"
        onMouseEnter={() => setVisible(true)}
        onMouseLeave={() => setVisible(false)}
        onClick={() => setVisible(!visible)}
      >
        ?
      </span>
      {visible && <span className="help-tooltip-bubble">{text}</span>}
    </span>
  );
}
