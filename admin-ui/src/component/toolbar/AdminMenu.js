import React from 'react';
import Button from '@material-ui/core/Button';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import { AccountTree } from '@material-ui/icons';
import { Link as RouterLink } from 'react-router-dom';

const Link = React.forwardRef((props, ref) => <RouterLink innerRef={ref} {...props} />);

function AdminMenu({ name, links }) {
  const [anchorEl, setAnchorEl] = React.useState(null);

  const handleClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  return (
    <>
      <Button onClick={handleClick} color="inherit">
        <AccountTree />
        {' '}
        {name}
      </Button>
      <Menu
        id={name}
        key={name}
        anchorEl={anchorEl}
        keepMounted
        open={Boolean(anchorEl)}
        onClose={handleClose}
      >

        {links.map((l) => (
          <MenuItem key={l.name} component={Link} to={l.to} onClick={handleClose}>
            {l.name}
          </MenuItem>
        ))}
      </Menu>
    </>
  );
}

export default AdminMenu;
